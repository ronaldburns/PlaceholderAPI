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
package me.rojo8399.placeholderapi.impl.placeholder.gen;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.I2B;
import static org.objectweb.asm.Opcodes.I2C;
import static org.objectweb.asm.Opcodes.I2S;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.api.world.Locatable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import me.rojo8399.placeholderapi.NoValueException;
import me.rojo8399.placeholderapi.Observer;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.Source;
import me.rojo8399.placeholderapi.Token;
import me.rojo8399.placeholderapi.impl.placeholder.Expansion;
import me.rojo8399.placeholderapi.impl.utils.TypeUtils;

/**
 * @author Wundero
 *
 */
public class ClassPlaceholderFactory {

	private AtomicInteger iid = new AtomicInteger();
	private String targetPackage;
	private DefineableClassLoader classLoader;

	private final LoadingCache<Method, Class<? extends Expansion<?, ?, ?>>> cache = CacheBuilder.newBuilder()
			.concurrencyLevel(1).weakValues().build(new CacheLoader<Method, Class<? extends Expansion<?, ?, ?>>>() {
				@Override
				public Class<? extends Expansion<?, ?, ?>> load(Method method) throws Exception {
					return createClass(method);
				}
			});

	public ClassPlaceholderFactory(String targetPackage, DefineableClassLoader loader) {
		checkNotNull(targetPackage, "targetPackage");
		checkArgument(!targetPackage.isEmpty(), "targetPackage cannot be empty");
		this.targetPackage = targetPackage + '.';
		this.classLoader = checkNotNull(loader, "classLoader");
	}

	public Expansion<?, ?, ?> create(Object handle, Method method) throws Exception {
		if (!Modifier.isPublic(handle.getClass().getModifiers())) {
			throw new IllegalArgumentException("Class must be public!");
		}
		if (!Modifier.isPublic(method.getModifiers())) {
			throw new IllegalArgumentException("Method must be public!");
		}
		return this.cache.get(method).getConstructor(method.getDeclaringClass()).newInstance(handle);
	}

	Class<? extends Expansion<?, ?, ?>> createClass(Method method) throws Exception {
		Class<?> handle = method.getDeclaringClass();
		if (!Modifier.isPublic(handle.getModifiers())) {
			throw new IllegalArgumentException("Class must be public!");
		}
		if (!Modifier.isPublic(method.getModifiers())) {
			throw new IllegalArgumentException("Method must be public!");
		}
		String id = method.getAnnotation(Placeholder.class).id();
		String name = this.targetPackage + id + "Placeholder_" + handle.getSimpleName() + "_" + method.getName()
				+ iid.incrementAndGet();
		byte[] bytes = generateClass(name, handle, method);
		/*
		Files.write(new File(name + ".class").toPath(), bytes);
		System.out.println("written " + name);
		*/
		return this.classLoader.defineClass(name, bytes);
	}

	private static final String OBJECT_NAME = Type.getInternalName(Object.class);
	private static final String STRING_NAME = Type.getInternalName(String.class);
	private static final String PLACEHOLDER_NAME = Type.getInternalName(InternalExpansion.class);
	private static final String OPT_NAME = Type.getInternalName(Optional.class);
	private static final String TLC_SIG, TRIM_SIG;

	private static final Map<Class<?>, Integer> order;

	static {
		order = new HashMap<>();
		order.put(Source.class, 0);
		order.put(Observer.class, 1);
		order.put(Token.class, 2);
		String f;
		try {
			f = Type.getMethodDescriptor(String.class.getMethod("toLowerCase"));
		} catch (Exception e) {
			f = "()Ljava/lang/String;";
		}
		TLC_SIG = f;
		try {
			f = Type.getMethodDescriptor(String.class.getMethod("trim"));
		} catch (Exception e) {
			f = "()Ljava/lang/String;";
		}
		TRIM_SIG = f;
	}

	private byte[] generateClass(String name, Class<?> handle, Method method) {
		name = name.replace(".", "/");
		final String handleName = Type.getInternalName(handle);
		final String handleDescriptor = Type.getDescriptor(handle);
		List<Parameter> pm = Arrays.asList(method.getParameters());
		boolean token = pm.stream().anyMatch(p -> p.isAnnotationPresent(Token.class));
		final boolean source = pm.stream().anyMatch(p -> p.isAnnotationPresent(Source.class));
		final boolean observer = pm.stream().anyMatch(p -> p.isAnnotationPresent(Observer.class));
		final boolean srcNullable = pm.stream().filter(p -> p.isAnnotationPresent(Source.class))
				.anyMatch(p -> p.isAnnotationPresent(Nullable.class));
		final boolean optionalTokenType = token && pm.stream().anyMatch(p -> p.getType().equals(Optional.class));
		final Optional<Class<?>> tokenClass = token
				? pm.stream().filter(p -> p.isAnnotationPresent(Token.class)).findAny().map(pr -> {
					if (optionalTokenType) {
						return (Class<?>) ((ParameterizedType) pr.getParameterizedType()).getActualTypeArguments()[0];
					} else {
						return pr.getType();
					}
				})
				: Optional.empty();
		token = token && tokenClass.isPresent();
		final boolean obsNullable = pm.stream().filter(p -> p.isAnnotationPresent(Observer.class))
				.anyMatch(p -> p.isAnnotationPresent(Nullable.class));
		final boolean tokNullable = token && !optionalTokenType && pm.stream()
				.filter(p -> p.isAnnotationPresent(Token.class)).anyMatch(p -> p.isAnnotationPresent(Nullable.class));
		final boolean fixToken = token && pm.stream().filter(p -> p.isAnnotationPresent(Token.class))
				.map(p -> p.getAnnotation(Token.class)).anyMatch(t -> t.fix());
		final Optional<Class<?>> sourceType = pm.stream().filter(p -> p.isAnnotationPresent(Source.class)).findFirst()
				.map(Parameter::getType);
		final Optional<Class<?>> observerType = pm.stream().filter(p -> p.isAnnotationPresent(Observer.class))
				.findFirst().map(Parameter::getType);
		Class<?> returnType = method.getReturnType();
		if (returnType.equals(Void.TYPE)) {
			returnType = Object.class;
		}
		String retString = Type.getDescriptor(returnType);
		String parseMethodDescriptor = "(L" + Type.getInternalName(sourceType.orElse(Locatable.class)) + ";L"
				+ Type.getInternalName(observerType.orElse(Locatable.class)) + ";L" + OPT_NAME + ";)" + retString;
		String externalParseDescriptor = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/util/Optional;)Ljava/lang/Object;";
		String methodDescriptor = Type.getMethodDescriptor(method);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		MethodVisitor mv;
		cw.visit(V1_8, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, name, null, PLACEHOLDER_NAME, null);
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", '(' + handleDescriptor + ")V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitLdcInsn(method.getAnnotation(Placeholder.class).id());
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESPECIAL, PLACEHOLDER_NAME, "<init>",
					"(L" + STRING_NAME + ";L" + OBJECT_NAME + ";)V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "parse", parseMethodDescriptor, null,
					new String[] { "java/lang/Exception" });
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, name, "handle", "L" + OBJECT_NAME + ";");
			mv.visitTypeInsn(CHECKCAST, handleName);
			if (token) {
				if (!String.class.isAssignableFrom(tokenClass.get()) || (tokenClass.get().isArray()
						&& String.class.isAssignableFrom(tokenClass.get().getComponentType()))) {
					mv.visitVarInsn(ALOAD, 3);
					mv.visitInsn(ACONST_NULL);
					mv.visitMethodInsn(INVOKEVIRTUAL, OPT_NAME, "orElse", "(Ljava/lang/Object;)Ljava/lang/Object;",
							false);
					if (optionalTokenType) {
						utilsTryCast(mv, boxedPrim(tokenClass.get()), false);
						mv.visitVarInsn(ASTORE, 3);
					} else {
						mv.visitVarInsn(ASTORE, 4);
						mv.visitVarInsn(ALOAD, 4);
						skipIfNull(mv, mv2 -> {
							mv2.visitVarInsn(ALOAD, 4);
							utilsTryCast(mv2, boxedPrim(tokenClass.get()), true);
							mv2.visitVarInsn(ASTORE, 4);
						});
					}
				} else {
					if (!optionalTokenType) {
						mv.visitVarInsn(ALOAD, 3);
						mv.visitInsn(ACONST_NULL);
						mv.visitMethodInsn(INVOKEVIRTUAL, OPT_NAME, "orElse", "(Ljava/lang/Object;)Ljava/lang/Object;",
								false);
						mv.visitVarInsn(ASTORE, 4);
						if (fixToken) {
							mv.visitVarInsn(ALOAD, 4);
							skipIfNull(mv, mv2 -> {
								mv2.visitVarInsn(ALOAD, 4);
								mv2.visitTypeInsn(CHECKCAST, STRING_NAME);
								mv2.visitMethodInsn(INVOKEVIRTUAL, STRING_NAME, "toLowerCase", TLC_SIG, false);
								mv2.visitMethodInsn(INVOKEVIRTUAL, STRING_NAME, "trim", TRIM_SIG, false);
								mv2.visitVarInsn(ASTORE, 4);
							});
						}
					}
				}
			}
			for (int i = 0; i < method.getParameterCount(); i++) {
				int x = 0;
				Parameter p = method.getParameters()[i];
				x = getOrder(p);
				if (x == 2 && !optionalTokenType) {
					x = 3;
				}
				mv.visitVarInsn(ALOAD, x + 1);
				boolean nullable = false;
				switch (x) {
				case 0:
					nullable = srcNullable;
					break;
				case 1:
					nullable = obsNullable;
					break;
				case 3:
					nullable = tokNullable;
					break;
				}
				final int y = x;
				nullCheck(mv, nullable, mv2 -> mv2.visitVarInsn(ALOAD, y + 1), x == 3 && token);
				mv.visitTypeInsn(CHECKCAST, Type.getInternalName(boxedPrim(p.getType())));
				boxToPrim(mv, p.getType(), y + 1);
			}
			mv.visitMethodInsn(INVOKEVIRTUAL, handleName, method.getName(), methodDescriptor, false);
			if (method.getReturnType().equals(Void.TYPE)) {
				mv.visitLdcInsn("");
			}
			if (!retString.startsWith("L")) {
				returnInsn(mv, returnType);
			} else {
				mv.visitInsn(ARETURN);
			}
			mv.visitMaxs(10, 10);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "parse", externalParseDescriptor, null,
					new String[] { "java/lang/Exception" });
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			if (sourceType.isPresent() && source) {
				mv.visitVarInsn(ALOAD, 1);
				tryCatch(mv, mv2 -> mv2.visitTypeInsn(CHECKCAST, Type.getInternalName(sourceType.get())), mv2 -> {
					mv2.visitLdcInsn("");
					mv2.visitInsn(ARETURN);
				});
			} else {
				mv.visitInsn(ACONST_NULL);
			}
			if (observerType.isPresent() && observer) {
				mv.visitVarInsn(ALOAD, 2);
				tryCatch(mv, mv2 -> mv2.visitTypeInsn(CHECKCAST, Type.getInternalName(observerType.get())), mv2 -> {
					mv2.visitLdcInsn("");
					mv2.visitInsn(ARETURN);
				});
			} else {
				mv.visitInsn(ACONST_NULL);
			}
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEVIRTUAL, name, "parse", parseMethodDescriptor, false);
			unboxFromPrim(mv, returnType);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		cw.visitEnd();
		return cw.toByteArray();
	}

	private static void unboxFromPrim(MethodVisitor mv, Class<?> p) {
		if (!p.isPrimitive()) {
			return;
		}
		String boxName = null, typeName = null;
		if (p.equals(int.class)) {
			boxName = "Integer";
			typeName = "I";
		}
		if (p.equals(char.class)) {
			boxName = "Character";
			typeName = "C";
		}
		if (p.equals(byte.class)) {
			boxName = "Byte";
			typeName = "B";
		}
		if (p.equals(boolean.class)) {
			boxName = "Boolean";
			typeName = "Z";
		}
		if (p.equals(long.class)) {
			boxName = "Long";
			typeName = "J";
		}
		if (p.equals(short.class)) {
			boxName = "Short";
			typeName = "S";
		}
		if (p.equals(float.class)) {
			boxName = "Float";
			typeName = "F";
		}
		if (p.equals(double.class)) {
			boxName = "Double";
			typeName = "D";
		}
		if (typeName == null || boxName == null) {
			return;
		}
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/" + boxName, "valueOf",
				"(" + typeName + ")Ljava/lang/" + boxName + ";", false);
	}

	private static void returnInsn(MethodVisitor mv, Class<?> clazz) {
		if (clazz.isPrimitive()) {
			if (clazz.equals(float.class)) {
				mv.visitInsn(FRETURN);
				return;
			}
			if (clazz.equals(long.class)) {
				mv.visitInsn(LRETURN);
				return;
			}
			if (clazz.equals(double.class)) {
				mv.visitInsn(DRETURN);
				return;
			}
			mv.visitInsn(IRETURN);
			return;
		}
		mv.visitInsn(ARETURN);
	}

	private static Class<?> boxedPrim(Class<?> primClass) {
		if (primClass.isPrimitive()) {
			if (primClass.equals(int.class)) {
				return Integer.class;
			}
			if (primClass.equals(char.class)) {
				return Character.class;
			}
			if (primClass.equals(byte.class)) {
				return Byte.class;
			}
			if (primClass.equals(boolean.class)) {
				return Boolean.class;
			}
			if (primClass.equals(long.class)) {
				return Long.class;
			}
			if (primClass.equals(short.class)) {
				return Short.class;
			}
			if (primClass.equals(float.class)) {
				return Float.class;
			}
			if (primClass.equals(double.class)) {
				return Double.class;
			}
		}
		return primClass;
	}

	private static void boxToPrim(MethodVisitor mv, Class<?> p, int varloc) {
		if (!p.isPrimitive()) {
			return;
		}
		String boxName = null, typeName = null, value = Type.getInternalName(p);
		int store = ISTORE, load = ILOAD;
		Consumer<MethodVisitor> converter = (mv2) -> {
		};
		if (p.equals(int.class)) {
			boxName = "Integer";
			typeName = "I";
		}
		if (p.equals(char.class)) {
			boxName = "Character";
			typeName = "C";
			converter = mv2 -> mv2.visitInsn(I2C);
		}
		if (p.equals(byte.class)) {
			boxName = "Byte";
			typeName = "B";
			converter = mv2 -> mv2.visitInsn(I2B);
		}
		if (p.equals(boolean.class)) {
			boxName = "Boolean";
			typeName = "Z";
			converter = mv2 -> mv2.visitInsn(I2B);
		}
		if (p.equals(long.class)) {
			boxName = "Long";
			typeName = "J";
			load = LLOAD;
			store = LSTORE;
		}
		if (p.equals(short.class)) {
			boxName = "Short";
			typeName = "S";
			converter = mv2 -> mv2.visitInsn(I2S);
		}
		if (p.equals(float.class)) {
			boxName = "Float";
			typeName = "F";
			load = FLOAD;
			store = FSTORE;
		}
		if (p.equals(double.class)) {
			boxName = "Double";
			typeName = "D";
			load = DLOAD;
			store = DSTORE;
		}
		if (typeName == null || boxName == null) {
			return;
		}
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/" + boxName, value + "Value", "()" + typeName, false);
		mv.visitVarInsn(store, varloc);
		mv.visitVarInsn(load, varloc);
		converter.accept(mv);
	}

	private static void utilsTryCast(MethodVisitor mv, Class<?> expected, boolean orNull) {
		// assume obj to cast is already on stack and only that obj is on stack
		mv.visitLdcInsn(Type.getType(expected));
		mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
		mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(TypeUtils.class), "tryCast",
				"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Boolean;)Ljava/util/Optional;", false);
		if (orNull) {
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKEVIRTUAL, OPT_NAME, "orElse", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
		}
	}

	private static void skipIfNull(MethodVisitor mv, Consumer<MethodVisitor> success) {
		// assume null check obj already loaded to stack
		Label no = new Label();
		mv.visitJumpInsn(IFNULL, no);
		success.accept(mv);
		mv.visitLabel(no);
	}

	private static void nullCheck(MethodVisitor mv, boolean nullable, Consumer<MethodVisitor> success,
			boolean throwError) {
		if (nullable) {
			return;
		}
		// assume null check obj already loaded to stack
		Label no = new Label();
		mv.visitJumpInsn(IFNONNULL, no);
		if (throwError) {
			mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(NoValueException.class));
			mv.visitVarInsn(ASTORE, 7);
			mv.visitVarInsn(ALOAD, 7);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(NoValueException.class), "<init>", "()V",
					false);
			mv.visitVarInsn(ALOAD, 7);
			mv.visitInsn(Opcodes.ATHROW);
		} else {
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ARETURN);
		}
		mv.visitLabel(no);
		success.accept(mv);
	}

	private static void tryCatch(MethodVisitor mv, Consumer<MethodVisitor> t, Consumer<MethodVisitor> c) {
		Label st = new Label(), et = new Label(), sc = new Label(), ec = new Label();
		mv.visitTryCatchBlock(st, et, sc, "java/lang/Exception");
		mv.visitLabel(st);
		t.accept(mv);
		mv.visitLabel(et);
		mv.visitJumpInsn(GOTO, ec);
		mv.visitLabel(sc);
		c.accept(mv);
		mv.visitLabel(ec);
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
