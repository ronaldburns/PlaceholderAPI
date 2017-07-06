/*
 The MIT License (MIT)

 Copyright (c) 2017 Wundero

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package me.rojo8399.placeholderapi.placeholder.gen;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import me.rojo8399.placeholderapi.placeholder.Observer;
import me.rojo8399.placeholderapi.placeholder.Placeholder;
import me.rojo8399.placeholderapi.placeholder.Source;
import me.rojo8399.placeholderapi.placeholder.Token;

/**
 * @author Wundero
 *
 */
public class ClassPlaceholderFactory {

	private AtomicInteger iid = new AtomicInteger();
	private String targetPackage;
	private DefineableClassLoader classLoader;

	private final LoadingCache<Method, Class<? extends PlaceholderContainer>> cache = CacheBuilder.newBuilder()
			.concurrencyLevel(1).weakValues().build(new CacheLoader<Method, Class<? extends PlaceholderContainer>>() {
				@Override
				public Class<? extends PlaceholderContainer> load(Method method) throws Exception {
					return createClass(method);
				}
			});

	public ClassPlaceholderFactory(String targetPackage, DefineableClassLoader loader) {
		checkNotNull(targetPackage, "targetPackage");
		checkArgument(!targetPackage.isEmpty(), "targetPackage cannot be empty");
		this.targetPackage = targetPackage + '.';
		this.classLoader = checkNotNull(loader, "classLoader");
	}

	public PlaceholderContainer create(Object handle, Method method) throws Exception {
		return this.cache.get(method).getConstructor(method.getDeclaringClass()).newInstance(handle);
	}

	Class<? extends PlaceholderContainer> createClass(Method method) throws Exception {
		Class<?> handle = method.getDeclaringClass();
		String id = method.getAnnotation(Placeholder.class).id();
		String name = this.targetPackage + id + "Placeholder_" + handle.getSimpleName() + "_" + method.getName()
				+ iid.incrementAndGet();
		byte[] bytes = generateClass(name, handle, method);
		return this.classLoader.defineClass(name, bytes);
	}

	private static final String OBJECT_NAME = Type.getInternalName(Object.class);
	private static final String PLACEHOLDER_NAME = Type.getInternalName(PlaceholderContainer.class);
	private static final String HANDLE_METHOD_DESCRIPTOR = "(" + Type.getDescriptor(PlaceholderData.class) + ")L"
			+ OBJECT_NAME + ";";
	private static final String DATA_NAME = Type.getInternalName(PlaceholderData.class);
	private static final String SRCTRG_DESC = "()L" + OBJECT_NAME + ";";
	private static final String TOK_DESC;
	private static final String OPT_NAME = Type.getInternalName(Optional.class);
	private static final String OPT_SIG;

	private static final Map<Class<?>, Integer> order;

	static {
		order = new HashMap<>();
		order.put(Source.class, 0);
		order.put(Observer.class, 1);
		order.put(Token.class, 2);
		SignatureVisitor sv = new SignatureWriter();
		SignatureVisitor psv = sv.visitParameterType();
		psv.visitClassType(Type.getInternalName(Optional.class));
		SignatureVisitor ppsv = psv.visitTypeArgument('=');
		ppsv.visitClassType(Type.getInternalName(String.class));
		psv.visitEnd();
		SignatureVisitor rtv = sv.visitReturnType();
		psv.visitClassType(Type.getInternalName(Optional.class));
		SignatureVisitor prtv = rtv.visitTypeArgument('=');
		ppsv.visitClassType(Type.getInternalName(String.class));
		OPT_SIG = sv.toString() + ";";
		String f;
		try {
			f = Type.getMethodDescriptor(PlaceholderData.class.getDeclaredMethod("token"));
		} catch (Exception e) {
			f = "()" + rtv.toString();
		}
		TOK_DESC = f;
	}

	private byte[] generateClass(String name, Class<?> handle, Method method) {
		name = name.replace(".", "/");
		final String handleName = Type.getInternalName(handle);
		final String handleDescriptor = Type.getDescriptor(handle);
		List<Parameter> pm = Arrays.asList(method.getParameters());
		final boolean token = pm.stream().anyMatch(p -> p.getAnnotation(Token.class) != null);
		final boolean optionalTokenType = pm.stream()
				.anyMatch(p -> p.getParameterizedType().getTypeName().equals("java.util.Optional<java.lang.String>"));
		String methodDescriptor = Type.getMethodDescriptor(method);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		MethodVisitor mv;
		cw.visit(V1_8, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, name, null, PLACEHOLDER_NAME, null);
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", '(' + handleDescriptor + ")V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESPECIAL, PLACEHOLDER_NAME, "<init>", "(L" + OBJECT_NAME + ";)V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "handle", HANDLE_METHOD_DESCRIPTOR, null,
					new String[] { "java/lang/Exception" });
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, name, "handle", "L" + OBJECT_NAME + ";");
			mv.visitTypeInsn(CHECKCAST, handleName);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, DATA_NAME, "token", TOK_DESC, false);
			mv.visitVarInsn(ASTORE, 6);
			mv.visitVarInsn(ALOAD, 6);
			if (!optionalTokenType && token) {
				mv.visitLdcInsn("");
				mv.visitMethodInsn(INVOKEVIRTUAL, OPT_NAME, "orElse", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
			}
			mv.visitVarInsn(ASTORE, 4);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, DATA_NAME, "target", SRCTRG_DESC, false);
			mv.visitVarInsn(ASTORE, 3);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, DATA_NAME, "source", SRCTRG_DESC, false);
			mv.visitVarInsn(ASTORE, 2);
			for (int i = 0; i < method.getParameterCount(); i++) {
				int x = 0;
				Parameter p = method.getParameters()[i];
				x = getOrder(p);
				mv.visitVarInsn(ALOAD, x + 2);
				mv.visitTypeInsn(CHECKCAST, Type.getInternalName(p.getType()));

			}
			mv.visitMethodInsn(INVOKEVIRTUAL, handleName, method.getName(), methodDescriptor, false);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		cw.visitEnd();
		return cw.toByteArray();
	}

	private static int getOrder(Parameter p) {
		for (java.lang.annotation.Annotation a : p.getAnnotations()) {
			if (order.containsKey(a.annotationType())) {
				return order.get(a.annotationType());
			}
		}
		return 0;
	}

}
