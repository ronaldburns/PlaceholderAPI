/*
                   GNU LESSER GENERAL PUBLIC LICENSE
                       Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.


  This version of the GNU Lesser General Public License incorporates
the terms and conditions of version 3 of the GNU General Public
License, supplemented by the additional permissions listed below.

  0. Additional Definitions.

  As used herein, "this License" refers to version 3 of the GNU Lesser
General Public License, and the "GNU GPL" refers to version 3 of the GNU
General Public License.

  "The Library" refers to a covered work governed by this License,
other than an Application or a Combined Work as defined below.

  An "Application" is any work that makes use of an interface provided
by the Library, but which is not otherwise based on the Library.
Defining a subclass of a class defined by the Library is deemed a mode
of using an interface provided by the Library.

  A "Combined Work" is a work produced by combining or linking an
Application with the Library.  The particular version of the Library
with which the Combined Work was made is also called the "Linked
Version".

  The "Minimal Corresponding Source" for a Combined Work means the
Corresponding Source for the Combined Work, excluding any source code
for portions of the Combined Work that, considered in isolation, are
based on the Application, and not on the Linked Version.

  The "Corresponding Application Code" for a Combined Work means the
object code and/or source code for the Application, including any data
and utility programs needed for reproducing the Combined Work from the
Application, but excluding the System Libraries of the Combined Work.

  1. Exception to Section 3 of the GNU GPL.

  You may convey a covered work under sections 3 and 4 of this License
without being bound by section 3 of the GNU GPL.

  2. Conveying Modified Versions.

  If you modify a copy of the Library, and, in your modifications, a
facility refers to a function or data to be supplied by an Application
that uses the facility (other than as an argument passed when the
facility is invoked), then you may convey a copy of the modified
version:

   a) under this License, provided that you make a good faith effort to
   ensure that, in the event an Application does not supply the
   function or data, the facility still operates, and performs
   whatever part of its purpose remains meaningful, or

   b) under the GNU GPL, with none of the additional permissions of
   this License applicable to that copy.

  3. Object Code Incorporating Material from Library Header Files.

  The object code form of an Application may incorporate material from
a header file that is part of the Library.  You may convey such object
code under terms of your choice, provided that, if the incorporated
material is not limited to numerical parameters, data structure
layouts and accessors, or small macros, inline functions and templates
(ten or fewer lines in length), you do both of the following:

   a) Give prominent notice with each copy of the object code that the
   Library is used in it and that the Library and its use are
   covered by this License.

   b) Accompany the object code with a copy of the GNU GPL and this license
   document.

  4. Combined Works.

  You may convey a Combined Work under terms of your choice that,
taken together, effectively do not restrict modification of the
portions of the Library contained in the Combined Work and reverse
engineering for debugging such modifications, if you also do each of
the following:

   a) Give prominent notice with each copy of the Combined Work that
   the Library is used in it and that the Library and its use are
   covered by this License.

   b) Accompany the Combined Work with a copy of the GNU GPL and this license
   document.

   c) For a Combined Work that displays copyright notices during
   execution, include the copyright notice for the Library among
   these notices, as well as a reference directing the user to the
   copies of the GNU GPL and this license document.

   d) Do one of the following:

       0) Convey the Minimal Corresponding Source under the terms of this
       License, and the Corresponding Application Code in a form
       suitable for, and under terms that permit, the user to
       recombine or relink the Application with a modified version of
       the Linked Version to produce a modified Combined Work, in the
       manner specified by section 6 of the GNU GPL for conveying
       Corresponding Source.

       1) Use a suitable shared library mechanism for linking with the
       Library.  A suitable mechanism is one that (a) uses at run time
       a copy of the Library already present on the user's computer
       system, and (b) will operate properly with a modified version
       of the Library that is interface-compatible with the Linked
       Version.

   e) Provide Installation Information, but only if you would otherwise
   be required to provide such information under section 6 of the
   GNU GPL, and only to the extent that such information is
   necessary to install and execute a modified version of the
   Combined Work produced by recombining or relinking the
   Application with a modified version of the Linked Version. (If
   you use option 4d0, the Installation Information must accompany
   the Minimal Corresponding Source and Corresponding Application
   Code. If you use option 4d1, you must provide the Installation
   Information in the manner specified by section 6 of the GNU GPL
   for conveying Corresponding Source.)

  5. Combined Libraries.

  You may place library facilities that are a work based on the
Library side by side in a single library together with other library
facilities that are not Applications and are not covered by this
License, and convey such a combined library under terms of your
choice, if you do both of the following:

   a) Accompany the combined library with a copy of the same work based
   on the Library, uncombined with any other library facilities,
   conveyed under the terms of this License.

   b) Give prominent notice with the combined library that part of it
   is a work based on the Library, and explaining where to find the
   accompanying uncombined form of the same work.

  6. Revised Versions of the GNU Lesser General Public License.

  The Free Software Foundation may publish revised and/or new versions
of the GNU Lesser General Public License from time to time. Such new
versions will be similar in spirit to the present version, but may
differ in detail to address new problems or concerns.

  Each version is given a distinguishing version number. If the
Library as you received it specifies that a certain numbered version
of the GNU Lesser General Public License "or any later version"
applies to it, you have the option of following the terms and
conditions either of that published version or of any later version
published by the Free Software Foundation. If the Library as you
received it does not specify a version number of the GNU Lesser
General Public License, you may choose any version of the GNU Lesser
General Public License ever published by the Free Software Foundation.

  If the Library as you received it specifies that a proxy can decide
whether future versions of the GNU Lesser General Public License shall
apply, that proxy's public statement of acceptance of any version is
permanent authorization for you to choose that version for the
Library.

 */
package me.rojo8399.placeholderapi.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

/**
 * bStats collects some data for plugin authors.
 *
 * Check out https://bStats.org/ to learn more about bStats!
 */
public class Metrics {

	/**
	 * Represents a custom advanced bar chart.
	 */
	public static abstract class AdvancedBarChart extends CustomChart {

		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public AdvancedBarChart(String chartId) {
			super(chartId);
		}

		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			HashMap<String, int[]> map = getValues(new HashMap<String, int[]>());
			if (map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			boolean allSkipped = true;
			for (Map.Entry<String, int[]> entry : map.entrySet()) {
				if (entry.getValue().length == 0) {
					continue; // Skip this invalid
				}
				allSkipped = false;
				JsonArray categoryValues = new JsonArray();
				for (int categoryValue : entry.getValue()) {
					categoryValues.add(new JsonPrimitive(categoryValue));
				}
				values.add(entry.getKey(), categoryValues);
			}
			if (allSkipped) {
				// Null = skip the chart
				return null;
			}
			data.add("values", values);
			return data;
		}

		/**
		 * Gets the value of the chart.
		 *
		 * @param valueMap
		 *            Just an empty map. The only reason it exists is to make your life
		 *            easier. You don't have to create a map yourself!
		 * @return The value of the chart.
		 */
		public abstract HashMap<String, int[]> getValues(HashMap<String, int[]> valueMap);

	}

	/**
	 * Represents a custom advanced map chart.
	 */
	public static abstract class AdvancedMapChart extends CustomChart {

		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public AdvancedMapChart(String chartId) {
			super(chartId);
		}

		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			HashMap<Country, Integer> map = getValues(new HashMap<Country, Integer>());
			if (map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			boolean allSkipped = true;
			for (Map.Entry<Country, Integer> entry : map.entrySet()) {
				if (entry.getValue() == 0) {
					continue; // Skip this invalid
				}
				allSkipped = false;
				values.addProperty(entry.getKey().getCountryIsoTag(), entry.getValue());
			}
			if (allSkipped) {
				// Null = skip the chart
				return null;
			}
			data.add("values", values);
			return data;
		}

		/**
		 * Gets the value of the chart.
		 *
		 * @param valueMap
		 *            Just an empty map. The only reason it exists is to make your life
		 *            easier. You don't have to create a map yourself!
		 * @return The value of the chart.
		 */
		public abstract HashMap<Country, Integer> getValues(HashMap<Country, Integer> valueMap);

	}

	/**
	 * Represents a custom advanced pie.
	 */
	public static abstract class AdvancedPie extends CustomChart {

		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public AdvancedPie(String chartId) {
			super(chartId);
		}

		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			HashMap<String, Integer> map = getValues(new HashMap<String, Integer>());
			if (map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			boolean allSkipped = true;
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				if (entry.getValue() == 0) {
					continue; // Skip this invalid
				}
				allSkipped = false;
				values.addProperty(entry.getKey(), entry.getValue());
			}
			if (allSkipped) {
				// Null = skip the chart
				return null;
			}
			data.add("values", values);
			return data;
		}

		/**
		 * Gets the values of the pie.
		 *
		 * @param valueMap
		 *            Just an empty map. The only reason it exists is to make your life
		 *            easier. You don't have to create a map yourself!
		 * @return The values of the pie.
		 */
		public abstract HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap);
	}

	/**
	 * A enum which is used for custom maps.
	 */
	public enum Country {

		AFGHANISTAN("AF", "Afghanistan"),

		ALAND_ISLANDS("AX", "Åland Islands"), ALBANIA("AL", "Albania"), ALGERIA("DZ", "Algeria"), AMERICAN_SAMOA("AS",
				"American Samoa"), ANDORRA("AD", "Andorra"), ANGOLA("AO", "Angola"), ANGUILLA("AI",
						"Anguilla"), ANTARCTICA("AQ", "Antarctica"), ANTIGUA_AND_BARBUDA("AG",
								"Antigua and Barbuda"), ARGENTINA("AR", "Argentina"), ARMENIA("AM", "Armenia"), ARUBA(
										"AW", "Aruba"), AUSTRALIA("AU", "Australia"), AUSTRIA("AT", "Austria"),
		/**
		 * bStats will use the country of the server.
		 */
		AUTO_DETECT("AUTO", "Auto Detected"), AZERBAIJAN("AZ", "Azerbaijan"), BAHAMAS("BS", "Bahamas"), BAHRAIN("BH",
				"Bahrain"), BANGLADESH("BD", "Bangladesh"), BARBADOS("BB", "Barbados"), BELARUS("BY",
						"Belarus"), BELGIUM("BE", "Belgium"), BELIZE("BZ", "Belize"), BENIN("BJ",
								"Benin"), BERMUDA("BM", "Bermuda"), BHUTAN("BT", "Bhutan"), BOLIVIA("BO",
										"Bolivia"), BONAIRE_SINT_EUSTATIUS_AND_SABA("BQ",
												"Bonaire, Sint Eustatius and Saba"), BOSNIA_AND_HERZEGOVINA("BA",
														"Bosnia and Herzegovina"), BOTSWANA("BW",
																"Botswana"), BOUVET_ISLAND("BV",
																		"Bouvet Island"), BRAZIL("BR",
																				"Brazil"), BRITISH_INDIAN_OCEAN_TERRITORY(
																						"IO",
																						"British Indian Ocean Territory"), BRITISH_VIRGIN_ISLANDS(
																								"VG",
																								"British Virgin Islands"), BRUNEI(
																										"BN",
																										"Brunei"), BULGARIA(
																												"BG",
																												"Bulgaria"), BURKINA_FASO(
																														"BF",
																														"Burkina Faso"), BURUNDI(
																																"BI",
																																"Burundi"), CAMBODIA(
																																		"KH",
																																		"Cambodia"), CAMEROON(
																																				"CM",
																																				"Cameroon"), CANADA(
																																						"CA",
																																						"Canada"), CAPE_VERDE(
																																								"CV",
																																								"Cape Verde"), CAYMAN_ISLANDS(
																																										"KY",
																																										"Cayman Islands"), CENTRAL_AFRICAN_REPUBLIC(
																																												"CF",
																																												"Central African Republic"), CHAD(
																																														"TD",
																																														"Chad"), CHILE(
																																																"CL",
																																																"Chile"), CHINA(
																																																		"CN",
																																																		"China"), CHRISTMAS_ISLAND(
																																																				"CX",
																																																				"Christmas Island"), COCOS_ISLANDS(
																																																						"CC",
																																																						"Cocos Islands"), COLOMBIA(
																																																								"CO",
																																																								"Colombia"), COMOROS(
																																																										"KM",
																																																										"Comoros"), CONGO(
																																																												"CG",
																																																												"Congo"), COOK_ISLANDS(
																																																														"CK",
																																																														"Cook Islands"), COSTA_RICA(
																																																																"CR",
																																																																"Costa Rica"), COTE_D_IVOIRE(
																																																																		"CI",
																																																																		"Côte d'Ivoire"), CROATIA(
																																																																				"HR",
																																																																				"Croatia"), CUBA(
																																																																						"CU",
																																																																						"Cuba"), CURACAO(
																																																																								"CW",
																																																																								"Curaçao"), CYPRUS(
																																																																										"CY",
																																																																										"Cyprus"), CZECH_REPUBLIC(
																																																																												"CZ",
																																																																												"Czech Republic"), DENMARK(
																																																																														"DK",
																																																																														"Denmark"), DJIBOUTI(
																																																																																"DJ",
																																																																																"Djibouti"), DOMINICA(
																																																																																		"DM",
																																																																																		"Dominica"), DOMINICAN_REPUBLIC(
																																																																																				"DO",
																																																																																				"Dominican Republic"), ECUADOR(
																																																																																						"EC",
																																																																																						"Ecuador"), EGYPT(
																																																																																								"EG",
																																																																																								"Egypt"), EL_SALVADOR(
																																																																																										"SV",
																																																																																										"El Salvador"), EQUATORIAL_GUINEA(
																																																																																												"GQ",
																																																																																												"Equatorial Guinea"), ERITREA(
																																																																																														"ER",
																																																																																														"Eritrea"), ESTONIA(
																																																																																																"EE",
																																																																																																"Estonia"), ETHIOPIA(
																																																																																																		"ET",
																																																																																																		"Ethiopia"), FALKLAND_ISLANDS(
																																																																																																				"FK",
																																																																																																				"Falkland Islands"), FAROE_ISLANDS(
																																																																																																						"FO",
																																																																																																						"Faroe Islands"), FIJI(
																																																																																																								"FJ",
																																																																																																								"Fiji"), FINLAND(
																																																																																																										"FI",
																																																																																																										"Finland"), FRANCE(
																																																																																																												"FR",
																																																																																																												"France"), FRENCH_GUIANA(
																																																																																																														"GF",
																																																																																																														"French Guiana"), FRENCH_POLYNESIA(
																																																																																																																"PF",
																																																																																																																"French Polynesia"), FRENCH_SOUTHERN_TERRITORIES(
																																																																																																																		"TF",
																																																																																																																		"French Southern Territories"), GABON(
																																																																																																																				"GA",
																																																																																																																				"Gabon"), GAMBIA(
																																																																																																																						"GM",
																																																																																																																						"Gambia"), GEORGIA(
																																																																																																																								"GE",
																																																																																																																								"Georgia"), GERMANY(
																																																																																																																										"DE",
																																																																																																																										"Germany"), GHANA(
																																																																																																																												"GH",
																																																																																																																												"Ghana"), GIBRALTAR(
																																																																																																																														"GI",
																																																																																																																														"Gibraltar"), GREECE(
																																																																																																																																"GR",
																																																																																																																																"Greece"), GREENLAND(
																																																																																																																																		"GL",
																																																																																																																																		"Greenland"), GRENADA(
																																																																																																																																				"GD",
																																																																																																																																				"Grenada"), GUADELOUPE(
																																																																																																																																						"GP",
																																																																																																																																						"Guadeloupe"), GUAM(
																																																																																																																																								"GU",
																																																																																																																																								"Guam"), GUATEMALA(
																																																																																																																																										"GT",
																																																																																																																																										"Guatemala"), GUERNSEY(
																																																																																																																																												"GG",
																																																																																																																																												"Guernsey"), GUINEA(
																																																																																																																																														"GN",
																																																																																																																																														"Guinea"), GUINEA_BISSAU(
																																																																																																																																																"GW",
																																																																																																																																																"Guinea-Bissau"), GUYANA(
																																																																																																																																																		"GY",
																																																																																																																																																		"Guyana"), HAITI(
																																																																																																																																																				"HT",
																																																																																																																																																				"Haiti"), HEARD_ISLAND_AND_MCDONALD_ISLANDS(
																																																																																																																																																						"HM",
																																																																																																																																																						"Heard Island And McDonald Islands"), HONDURAS(
																																																																																																																																																								"HN",
																																																																																																																																																								"Honduras"), HONG_KONG(
																																																																																																																																																										"HK",
																																																																																																																																																										"Hong Kong"), HUNGARY(
																																																																																																																																																												"HU",
																																																																																																																																																												"Hungary"), ICELAND(
																																																																																																																																																														"IS",
																																																																																																																																																														"Iceland"), INDIA(
																																																																																																																																																																"IN",
																																																																																																																																																																"India"), INDONESIA(
																																																																																																																																																																		"ID",
																																																																																																																																																																		"Indonesia"), IRAN(
																																																																																																																																																																				"IR",
																																																																																																																																																																				"Iran"), IRAQ(
																																																																																																																																																																						"IQ",
																																																																																																																																																																						"Iraq"), IRELAND(
																																																																																																																																																																								"IE",
																																																																																																																																																																								"Ireland"), ISLE_OF_MAN(
																																																																																																																																																																										"IM",
																																																																																																																																																																										"Isle Of Man"), ISRAEL(
																																																																																																																																																																												"IL",
																																																																																																																																																																												"Israel"), ITALY(
																																																																																																																																																																														"IT",
																																																																																																																																																																														"Italy"), JAMAICA(
																																																																																																																																																																																"JM",
																																																																																																																																																																																"Jamaica"), JAPAN(
																																																																																																																																																																																		"JP",
																																																																																																																																																																																		"Japan"), JERSEY(
																																																																																																																																																																																				"JE",
																																																																																																																																																																																				"Jersey"), JORDAN(
																																																																																																																																																																																						"JO",
																																																																																																																																																																																						"Jordan"), KAZAKHSTAN(
																																																																																																																																																																																								"KZ",
																																																																																																																																																																																								"Kazakhstan"), KENYA(
																																																																																																																																																																																										"KE",
																																																																																																																																																																																										"Kenya"), KIRIBATI(
																																																																																																																																																																																												"KI",
																																																																																																																																																																																												"Kiribati"), KUWAIT(
																																																																																																																																																																																														"KW",
																																																																																																																																																																																														"Kuwait"), KYRGYZSTAN(
																																																																																																																																																																																																"KG",
																																																																																																																																																																																																"Kyrgyzstan"), LAOS(
																																																																																																																																																																																																		"LA",
																																																																																																																																																																																																		"Laos"), LATVIA(
																																																																																																																																																																																																				"LV",
																																																																																																																																																																																																				"Latvia"), LEBANON(
																																																																																																																																																																																																						"LB",
																																																																																																																																																																																																						"Lebanon"), LESOTHO(
																																																																																																																																																																																																								"LS",
																																																																																																																																																																																																								"Lesotho"), LIBERIA(
																																																																																																																																																																																																										"LR",
																																																																																																																																																																																																										"Liberia"), LIBYA(
																																																																																																																																																																																																												"LY",
																																																																																																																																																																																																												"Libya"), LIECHTENSTEIN(
																																																																																																																																																																																																														"LI",
																																																																																																																																																																																																														"Liechtenstein"), LITHUANIA(
																																																																																																																																																																																																																"LT",
																																																																																																																																																																																																																"Lithuania"), LUXEMBOURG(
																																																																																																																																																																																																																		"LU",
																																																																																																																																																																																																																		"Luxembourg"), MACAO(
																																																																																																																																																																																																																				"MO",
																																																																																																																																																																																																																				"Macao"), MACEDONIA(
																																																																																																																																																																																																																						"MK",
																																																																																																																																																																																																																						"Macedonia"), MADAGASCAR(
																																																																																																																																																																																																																								"MG",
																																																																																																																																																																																																																								"Madagascar"), MALAWI(
																																																																																																																																																																																																																										"MW",
																																																																																																																																																																																																																										"Malawi"), MALAYSIA(
																																																																																																																																																																																																																												"MY",
																																																																																																																																																																																																																												"Malaysia"), MALDIVES(
																																																																																																																																																																																																																														"MV",
																																																																																																																																																																																																																														"Maldives"), MALI(
																																																																																																																																																																																																																																"ML",
																																																																																																																																																																																																																																"Mali"), MALTA(
																																																																																																																																																																																																																																		"MT",
																																																																																																																																																																																																																																		"Malta"), MARSHALL_ISLANDS(
																																																																																																																																																																																																																																				"MH",
																																																																																																																																																																																																																																				"Marshall Islands"), MARTINIQUE(
																																																																																																																																																																																																																																						"MQ",
																																																																																																																																																																																																																																						"Martinique"), MAURITANIA(
																																																																																																																																																																																																																																								"MR",
																																																																																																																																																																																																																																								"Mauritania"), MAURITIUS(
																																																																																																																																																																																																																																										"MU",
																																																																																																																																																																																																																																										"Mauritius"), MAYOTTE(
																																																																																																																																																																																																																																												"YT",
																																																																																																																																																																																																																																												"Mayotte"), MEXICO(
																																																																																																																																																																																																																																														"MX",
																																																																																																																																																																																																																																														"Mexico"), MICRONESIA(
																																																																																																																																																																																																																																																"FM",
																																																																																																																																																																																																																																																"Micronesia"), MOLDOVA(
																																																																																																																																																																																																																																																		"MD",
																																																																																																																																																																																																																																																		"Moldova"), MONACO(
																																																																																																																																																																																																																																																				"MC",
																																																																																																																																																																																																																																																				"Monaco"), MONGOLIA(
																																																																																																																																																																																																																																																						"MN",
																																																																																																																																																																																																																																																						"Mongolia"), MONTENEGRO(
																																																																																																																																																																																																																																																								"ME",
																																																																																																																																																																																																																																																								"Montenegro"), MONTSERRAT(
																																																																																																																																																																																																																																																										"MS",
																																																																																																																																																																																																																																																										"Montserrat"), MOROCCO(
																																																																																																																																																																																																																																																												"MA",
																																																																																																																																																																																																																																																												"Morocco"), MOZAMBIQUE(
																																																																																																																																																																																																																																																														"MZ",
																																																																																																																																																																																																																																																														"Mozambique"), MYANMAR(
																																																																																																																																																																																																																																																																"MM",
																																																																																																																																																																																																																																																																"Myanmar"), NAMIBIA(
																																																																																																																																																																																																																																																																		"NA",
																																																																																																																																																																																																																																																																		"Namibia"), NAURU(
																																																																																																																																																																																																																																																																				"NR",
																																																																																																																																																																																																																																																																				"Nauru"), NEPAL(
																																																																																																																																																																																																																																																																						"NP",
																																																																																																																																																																																																																																																																						"Nepal"), NETHERLANDS(
																																																																																																																																																																																																																																																																								"NL",
																																																																																																																																																																																																																																																																								"Netherlands"), NETHERLANDS_ANTILLES(
																																																																																																																																																																																																																																																																										"AN",
																																																																																																																																																																																																																																																																										"Netherlands Antilles"), NEW_CALEDONIA(
																																																																																																																																																																																																																																																																												"NC",
																																																																																																																																																																																																																																																																												"New Caledonia"), NEW_ZEALAND(
																																																																																																																																																																																																																																																																														"NZ",
																																																																																																																																																																																																																																																																														"New Zealand"), NICARAGUA(
																																																																																																																																																																																																																																																																																"NI",
																																																																																																																																																																																																																																																																																"Nicaragua"), NIGER(
																																																																																																																																																																																																																																																																																		"NE",
																																																																																																																																																																																																																																																																																		"Niger"), NIGERIA(
																																																																																																																																																																																																																																																																																				"NG",
																																																																																																																																																																																																																																																																																				"Nigeria"), NIUE(
																																																																																																																																																																																																																																																																																						"NU",
																																																																																																																																																																																																																																																																																						"Niue"), NORFOLK_ISLAND(
																																																																																																																																																																																																																																																																																								"NF",
																																																																																																																																																																																																																																																																																								"Norfolk Island"), NORTH_KOREA(
																																																																																																																																																																																																																																																																																										"KP",
																																																																																																																																																																																																																																																																																										"North Korea"), NORTHERN_MARIANA_ISLANDS(
																																																																																																																																																																																																																																																																																												"MP",
																																																																																																																																																																																																																																																																																												"Northern Mariana Islands"), NORWAY(
																																																																																																																																																																																																																																																																																														"NO",
																																																																																																																																																																																																																																																																																														"Norway"), OMAN(
																																																																																																																																																																																																																																																																																																"OM",
																																																																																																																																																																																																																																																																																																"Oman"), PAKISTAN(
																																																																																																																																																																																																																																																																																																		"PK",
																																																																																																																																																																																																																																																																																																		"Pakistan"), PALAU(
																																																																																																																																																																																																																																																																																																				"PW",
																																																																																																																																																																																																																																																																																																				"Palau"), PALESTINE(
																																																																																																																																																																																																																																																																																																						"PS",
																																																																																																																																																																																																																																																																																																						"Palestine"), PANAMA(
																																																																																																																																																																																																																																																																																																								"PA",
																																																																																																																																																																																																																																																																																																								"Panama"), PAPUA_NEW_GUINEA(
																																																																																																																																																																																																																																																																																																										"PG",
																																																																																																																																																																																																																																																																																																										"Papua New Guinea"), PARAGUAY(
																																																																																																																																																																																																																																																																																																												"PY",
																																																																																																																																																																																																																																																																																																												"Paraguay"), PERU(
																																																																																																																																																																																																																																																																																																														"PE",
																																																																																																																																																																																																																																																																																																														"Peru"), PHILIPPINES(
																																																																																																																																																																																																																																																																																																																"PH",
																																																																																																																																																																																																																																																																																																																"Philippines"), PITCAIRN(
																																																																																																																																																																																																																																																																																																																		"PN",
																																																																																																																																																																																																																																																																																																																		"Pitcairn"), POLAND(
																																																																																																																																																																																																																																																																																																																				"PL",
																																																																																																																																																																																																																																																																																																																				"Poland"), PORTUGAL(
																																																																																																																																																																																																																																																																																																																						"PT",
																																																																																																																																																																																																																																																																																																																						"Portugal"), PUERTO_RICO(
																																																																																																																																																																																																																																																																																																																								"PR",
																																																																																																																																																																																																																																																																																																																								"Puerto Rico"), QATAR(
																																																																																																																																																																																																																																																																																																																										"QA",
																																																																																																																																																																																																																																																																																																																										"Qatar"), REUNION(
																																																																																																																																																																																																																																																																																																																												"RE",
																																																																																																																																																																																																																																																																																																																												"Reunion"), ROMANIA(
																																																																																																																																																																																																																																																																																																																														"RO",
																																																																																																																																																																																																																																																																																																																														"Romania"), RUSSIA(
																																																																																																																																																																																																																																																																																																																																"RU",
																																																																																																																																																																																																																																																																																																																																"Russia"), RWANDA(
																																																																																																																																																																																																																																																																																																																																		"RW",
																																																																																																																																																																																																																																																																																																																																		"Rwanda"), SAINT_BARTHELEMY(
																																																																																																																																																																																																																																																																																																																																				"BL",
																																																																																																																																																																																																																																																																																																																																				"Saint Barthélemy"), SAINT_HELENA(
																																																																																																																																																																																																																																																																																																																																						"SH",
																																																																																																																																																																																																																																																																																																																																						"Saint Helena"), SAINT_KITTS_AND_NEVIS(
																																																																																																																																																																																																																																																																																																																																								"KN",
																																																																																																																																																																																																																																																																																																																																								"Saint Kitts And Nevis"), SAINT_LUCIA(
																																																																																																																																																																																																																																																																																																																																										"LC",
																																																																																																																																																																																																																																																																																																																																										"Saint Lucia"), SAINT_MARTIN(
																																																																																																																																																																																																																																																																																																																																												"MF",
																																																																																																																																																																																																																																																																																																																																												"Saint Martin"), SAINT_PIERRE_AND_MIQUELON(
																																																																																																																																																																																																																																																																																																																																														"PM",
																																																																																																																																																																																																																																																																																																																																														"Saint Pierre And Miquelon"), SAINT_VINCENT_AND_THE_GRENADINES(
																																																																																																																																																																																																																																																																																																																																																"VC",
																																																																																																																																																																																																																																																																																																																																																"Saint Vincent And The Grenadines"), SAMOA(
																																																																																																																																																																																																																																																																																																																																																		"WS",
																																																																																																																																																																																																																																																																																																																																																		"Samoa"), SAN_MARINO(
																																																																																																																																																																																																																																																																																																																																																				"SM",
																																																																																																																																																																																																																																																																																																																																																				"San Marino"), SAO_TOME_AND_PRINCIPE(
																																																																																																																																																																																																																																																																																																																																																						"ST",
																																																																																																																																																																																																																																																																																																																																																						"Sao Tome And Principe"), SAUDI_ARABIA(
																																																																																																																																																																																																																																																																																																																																																								"SA",
																																																																																																																																																																																																																																																																																																																																																								"Saudi Arabia"), SENEGAL(
																																																																																																																																																																																																																																																																																																																																																										"SN",
																																																																																																																																																																																																																																																																																																																																																										"Senegal"), SERBIA(
																																																																																																																																																																																																																																																																																																																																																												"RS",
																																																																																																																																																																																																																																																																																																																																																												"Serbia"), SEYCHELLES(
																																																																																																																																																																																																																																																																																																																																																														"SC",
																																																																																																																																																																																																																																																																																																																																																														"Seychelles"), SIERRA_LEONE(
																																																																																																																																																																																																																																																																																																																																																																"SL",
																																																																																																																																																																																																																																																																																																																																																																"Sierra Leone"), SINGAPORE(
																																																																																																																																																																																																																																																																																																																																																																		"SG",
																																																																																																																																																																																																																																																																																																																																																																		"Singapore"), SINT_MAARTEN_DUTCH_PART(
																																																																																																																																																																																																																																																																																																																																																																				"SX",
																																																																																																																																																																																																																																																																																																																																																																				"Sint Maarten (Dutch part)"), SLOVAKIA(
																																																																																																																																																																																																																																																																																																																																																																						"SK",
																																																																																																																																																																																																																																																																																																																																																																						"Slovakia"), SLOVENIA(
																																																																																																																																																																																																																																																																																																																																																																								"SI",
																																																																																																																																																																																																																																																																																																																																																																								"Slovenia"), SOLOMON_ISLANDS(
																																																																																																																																																																																																																																																																																																																																																																										"SB",
																																																																																																																																																																																																																																																																																																																																																																										"Solomon Islands"), SOMALIA(
																																																																																																																																																																																																																																																																																																																																																																												"SO",
																																																																																																																																																																																																																																																																																																																																																																												"Somalia"), SOUTH_AFRICA(
																																																																																																																																																																																																																																																																																																																																																																														"ZA",
																																																																																																																																																																																																																																																																																																																																																																														"South Africa"), SOUTH_GEORGIA_AND_THE_SOUTH_SANDWICH_ISLANDS(
																																																																																																																																																																																																																																																																																																																																																																																"GS",
																																																																																																																																																																																																																																																																																																																																																																																"South Georgia And The South Sandwich Islands"), SOUTH_KOREA(
																																																																																																																																																																																																																																																																																																																																																																																		"KR",
																																																																																																																																																																																																																																																																																																																																																																																		"South Korea"), SOUTH_SUDAN(
																																																																																																																																																																																																																																																																																																																																																																																				"SS",
																																																																																																																																																																																																																																																																																																																																																																																				"South Sudan"), SPAIN(
																																																																																																																																																																																																																																																																																																																																																																																						"ES",
																																																																																																																																																																																																																																																																																																																																																																																						"Spain"), SRI_LANKA(
																																																																																																																																																																																																																																																																																																																																																																																								"LK",
																																																																																																																																																																																																																																																																																																																																																																																								"Sri Lanka"), SUDAN(
																																																																																																																																																																																																																																																																																																																																																																																										"SD",
																																																																																																																																																																																																																																																																																																																																																																																										"Sudan"), SURINAME(
																																																																																																																																																																																																																																																																																																																																																																																												"SR",
																																																																																																																																																																																																																																																																																																																																																																																												"Suriname"), SVALBARD_AND_JAN_MAYEN(
																																																																																																																																																																																																																																																																																																																																																																																														"SJ",
																																																																																																																																																																																																																																																																																																																																																																																														"Svalbard And Jan Mayen"), SWAZILAND(
																																																																																																																																																																																																																																																																																																																																																																																																"SZ",
																																																																																																																																																																																																																																																																																																																																																																																																"Swaziland"), SWEDEN(
																																																																																																																																																																																																																																																																																																																																																																																																		"SE",
																																																																																																																																																																																																																																																																																																																																																																																																		"Sweden"), SWITZERLAND(
																																																																																																																																																																																																																																																																																																																																																																																																				"CH",
																																																																																																																																																																																																																																																																																																																																																																																																				"Switzerland"), SYRIA(
																																																																																																																																																																																																																																																																																																																																																																																																						"SY",
																																																																																																																																																																																																																																																																																																																																																																																																						"Syria"), TAIWAN(
																																																																																																																																																																																																																																																																																																																																																																																																								"TW",
																																																																																																																																																																																																																																																																																																																																																																																																								"Taiwan"), TAJIKISTAN(
																																																																																																																																																																																																																																																																																																																																																																																																										"TJ",
																																																																																																																																																																																																																																																																																																																																																																																																										"Tajikistan"), TANZANIA(
																																																																																																																																																																																																																																																																																																																																																																																																												"TZ",
																																																																																																																																																																																																																																																																																																																																																																																																												"Tanzania"), THAILAND(
																																																																																																																																																																																																																																																																																																																																																																																																														"TH",
																																																																																																																																																																																																																																																																																																																																																																																																														"Thailand"), THE_DEMOCRATIC_REPUBLIC_OF_CONGO(
																																																																																																																																																																																																																																																																																																																																																																																																																"CD",
																																																																																																																																																																																																																																																																																																																																																																																																																"The Democratic Republic Of Congo"), TIMOR_LESTE(
																																																																																																																																																																																																																																																																																																																																																																																																																		"TL",
																																																																																																																																																																																																																																																																																																																																																																																																																		"Timor-Leste"), TOGO(
																																																																																																																																																																																																																																																																																																																																																																																																																				"TG",
																																																																																																																																																																																																																																																																																																																																																																																																																				"Togo"), TOKELAU(
																																																																																																																																																																																																																																																																																																																																																																																																																						"TK",
																																																																																																																																																																																																																																																																																																																																																																																																																						"Tokelau"), TONGA(
																																																																																																																																																																																																																																																																																																																																																																																																																								"TO",
																																																																																																																																																																																																																																																																																																																																																																																																																								"Tonga"), TRINIDAD_AND_TOBAGO(
																																																																																																																																																																																																																																																																																																																																																																																																																										"TT",
																																																																																																																																																																																																																																																																																																																																																																																																																										"Trinidad and Tobago"), TUNISIA(
																																																																																																																																																																																																																																																																																																																																																																																																																												"TN",
																																																																																																																																																																																																																																																																																																																																																																																																																												"Tunisia"), TURKEY(
																																																																																																																																																																																																																																																																																																																																																																																																																														"TR",
																																																																																																																																																																																																																																																																																																																																																																																																																														"Turkey"), TURKMENISTAN(
																																																																																																																																																																																																																																																																																																																																																																																																																																"TM",
																																																																																																																																																																																																																																																																																																																																																																																																																																"Turkmenistan"), TURKS_AND_CAICOS_ISLANDS(
																																																																																																																																																																																																																																																																																																																																																																																																																																		"TC",
																																																																																																																																																																																																																																																																																																																																																																																																																																		"Turks And Caicos Islands"), TUVALU(
																																																																																																																																																																																																																																																																																																																																																																																																																																				"TV",
																																																																																																																																																																																																																																																																																																																																																																																																																																				"Tuvalu"), U_S__VIRGIN_ISLANDS(
																																																																																																																																																																																																																																																																																																																																																																																																																																						"VI",
																																																																																																																																																																																																																																																																																																																																																																																																																																						"U.S. Virgin Islands"), UGANDA(
																																																																																																																																																																																																																																																																																																																																																																																																																																								"UG",
																																																																																																																																																																																																																																																																																																																																																																																																																																								"Uganda"), UKRAINE(
																																																																																																																																																																																																																																																																																																																																																																																																																																										"UA",
																																																																																																																																																																																																																																																																																																																																																																																																																																										"Ukraine"), UNITED_ARAB_EMIRATES(
																																																																																																																																																																																																																																																																																																																																																																																																																																												"AE",
																																																																																																																																																																																																																																																																																																																																																																																																																																												"United Arab Emirates"), UNITED_KINGDOM(
																																																																																																																																																																																																																																																																																																																																																																																																																																														"GB",
																																																																																																																																																																																																																																																																																																																																																																																																																																														"United Kingdom"), UNITED_STATES(
																																																																																																																																																																																																																																																																																																																																																																																																																																																"US",
																																																																																																																																																																																																																																																																																																																																																																																																																																																"United States"), UNITED_STATES_MINOR_OUTLYING_ISLANDS(
																																																																																																																																																																																																																																																																																																																																																																																																																																																		"UM",
																																																																																																																																																																																																																																																																																																																																																																																																																																																		"United States Minor Outlying Islands"), URUGUAY(
																																																																																																																																																																																																																																																																																																																																																																																																																																																				"UY",
																																																																																																																																																																																																																																																																																																																																																																																																																																																				"Uruguay"), UZBEKISTAN(
																																																																																																																																																																																																																																																																																																																																																																																																																																																						"UZ",
																																																																																																																																																																																																																																																																																																																																																																																																																																																						"Uzbekistan"), VANUATU(
																																																																																																																																																																																																																																																																																																																																																																																																																																																								"VU",
																																																																																																																																																																																																																																																																																																																																																																																																																																																								"Vanuatu"), VATICAN(
																																																																																																																																																																																																																																																																																																																																																																																																																																																										"VA",
																																																																																																																																																																																																																																																																																																																																																																																																																																																										"Vatican"), VENEZUELA(
																																																																																																																																																																																																																																																																																																																																																																																																																																																												"VE",
																																																																																																																																																																																																																																																																																																																																																																																																																																																												"Venezuela"), VIETNAM(
																																																																																																																																																																																																																																																																																																																																																																																																																																																														"VN",
																																																																																																																																																																																																																																																																																																																																																																																																																																																														"Vietnam"), WALLIS_AND_FUTUNA(
																																																																																																																																																																																																																																																																																																																																																																																																																																																																"WF",
																																																																																																																																																																																																																																																																																																																																																																																																																																																																"Wallis And Futuna"), WESTERN_SAHARA(
																																																																																																																																																																																																																																																																																																																																																																																																																																																																		"EH",
																																																																																																																																																																																																																																																																																																																																																																																																																																																																		"Western Sahara"), YEMEN(
																																																																																																																																																																																																																																																																																																																																																																																																																																																																				"YE",
																																																																																																																																																																																																																																																																																																																																																																																																																																																																				"Yemen"), ZAMBIA(
																																																																																																																																																																																																																																																																																																																																																																																																																																																																						"ZM",
																																																																																																																																																																																																																																																																																																																																																																																																																																																																						"Zambia"), ZIMBABWE(
																																																																																																																																																																																																																																																																																																																																																																																																																																																																								"ZW",
																																																																																																																																																																																																																																																																																																																																																																																																																																																																								"Zimbabwe");

		/**
		 * Gets a country by it's iso tag.
		 *
		 * @param isoTag
		 *            The iso tag of the county.
		 * @return The country with the given iso tag or <code>null</code> if unknown.
		 */
		public static Country byIsoTag(String isoTag) {
			for (Country country : Country.values()) {
				if (country.getCountryIsoTag().equals(isoTag)) {
					return country;
				}
			}
			return null;
		}

		/**
		 * Gets a country by a locale.
		 *
		 * @param locale
		 *            The locale.
		 * @return The country from the giben locale or <code>null</code> if unknown
		 *         country or if the locale does not contain a country.
		 */
		public static Country byLocale(Locale locale) {
			return byIsoTag(locale.getCountry());
		}

		private String isoTag;

		private String name;

		Country(String isoTag, String name) {
			this.isoTag = isoTag;
			this.name = name;
		}

		/**
		 * Gets the iso tag of the country.
		 *
		 * @return The iso tag of the country.
		 */
		public String getCountryIsoTag() {
			return isoTag;
		}

		/**
		 * Gets the name of the country.
		 *
		 * @return The name of the country.
		 */
		public String getCountryName() {
			return name;
		}

	}

	/**
	 * Represents a custom chart.
	 */
	public static abstract class CustomChart {

		// The id of the chart
		protected final String chartId;

		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public CustomChart(String chartId) {
			if (chartId == null || chartId.isEmpty()) {
				throw new IllegalArgumentException("ChartId cannot be null or empty!");
			}
			this.chartId = chartId;
		}

		protected abstract JsonObject getChartData();

		protected JsonObject getRequestJsonObject(Logger logger, boolean logFailedRequests) {
			JsonObject chart = new JsonObject();
			chart.addProperty("chartId", chartId);
			try {
				JsonObject data = getChartData();
				if (data == null) {
					// If the data is null we don't send the chart.
					return null;
				}
				chart.add("data", data);
			} catch (Throwable t) {
				if (logFailedRequests) {
					logger.warn("Failed to get data for custom chart with id {}", chartId, t);
				}
				return null;
			}
			return chart;
		}

	}

	/**
	 * Represents a custom multi line chart.
	 */
	public static abstract class MultiLineChart extends CustomChart {

		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public MultiLineChart(String chartId) {
			super(chartId);
		}

		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			HashMap<String, Integer> map = getValues(new HashMap<String, Integer>());
			if (map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			boolean allSkipped = true;
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				if (entry.getValue() == 0) {
					continue; // Skip this invalid
				}
				allSkipped = false;
				values.addProperty(entry.getKey(), entry.getValue());
			}
			if (allSkipped) {
				// Null = skip the chart
				return null;
			}
			data.add("values", values);
			return data;
		}

		/**
		 * Gets the values of the chart.
		 *
		 * @param valueMap
		 *            Just an empty map. The only reason it exists is to make your life
		 *            easier. You don't have to create a map yourself!
		 * @return The values of the chart.
		 */
		public abstract HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap);

	}

	/**
	 * Represents a custom simple bar chart.
	 */
	public static abstract class SimpleBarChart extends CustomChart {

		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public SimpleBarChart(String chartId) {
			super(chartId);
		}

		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			HashMap<String, Integer> map = getValues(new HashMap<String, Integer>());
			if (map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				JsonArray categoryValues = new JsonArray();
				categoryValues.add(new JsonPrimitive(entry.getValue()));
				values.add(entry.getKey(), categoryValues);
			}
			data.add("values", values);
			return data;
		}

		/**
		 * Gets the value of the chart.
		 *
		 * @param valueMap
		 *            Just an empty map. The only reason it exists is to make your life
		 *            easier. You don't have to create a map yourself!
		 * @return The value of the chart.
		 */
		public abstract HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap);

	}

	/**
	 * Represents a custom simple map chart.
	 */
	public static abstract class SimpleMapChart extends CustomChart {

		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public SimpleMapChart(String chartId) {
			super(chartId);
		}

		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			Country value = getValue();

			if (value == null) {
				// Null = skip the chart
				return null;
			}
			data.addProperty("value", value.getCountryIsoTag());
			return data;
		}

		/**
		 * Gets the value of the chart.
		 *
		 * @return The value of the chart.
		 */
		public abstract Country getValue();

	}

	/**
	 * Represents a custom simple pie.
	 */
	public static abstract class SimplePie extends CustomChart {

		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public SimplePie(String chartId) {
			super(chartId);
		}

		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			String value = getValue();
			if (value == null || value.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			data.addProperty("value", value);
			return data;
		}

		/**
		 * Gets the value of the pie.
		 *
		 * @return The value of the pie.
		 */
		public abstract String getValue();
	}

	/**
	 * Represents a custom single line chart.
	 */
	public static abstract class SingleLineChart extends CustomChart {

		/**
		 * Class constructor.
		 *
		 * @param chartId
		 *            The id of the chart.
		 */
		public SingleLineChart(String chartId) {
			super(chartId);
		}

		@Override
		protected JsonObject getChartData() {
			JsonObject data = new JsonObject();
			int value = getValue();
			if (value == 0) {
				// Null = skip the chart
				return null;
			}
			data.addProperty("value", value);
			return data;
		}

		/**
		 * Gets the value of the chart.
		 *
		 * @return The value of the chart.
		 */
		public abstract int getValue();

	}

	// The version of this bStats class
	public static final int B_STATS_VERSION = 1;

	// We use this flag to ensure only one instance of this class exist
	private static boolean created = false;

	// A list with all known metrics class objects including this one
	private static final List<Object> knownMetricsInstances = new ArrayList<>();

	// The url to which the data is sent
	private static final String URL = "https://bStats.org/submitData/sponge";

	static {
		// Maven's Relocate is clever and changes strings, too. So we have to
		// use this little "trick" ... :D
		final String defaultPackage = new String(new byte[] { 'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's' });
		final String examplePackage = new String(
				new byte[] { 'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e' });
		// We want to make sure nobody just copy & pastes the example and use
		// the wrong package names
		if (Metrics.class.getPackage().getName().equals(defaultPackage)
				|| Metrics.class.getPackage().getName().equals(examplePackage)) {
			throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
		}
	}

	/**
	 * Gzips the given String.
	 *
	 * @param str
	 *            The string to gzip.
	 * @return The gzipped String.
	 * @throws IOException
	 *             If the compression failed.
	 */
	private static byte[] compress(final String str) throws IOException {
		if (str == null) {
			return null;
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
		gzip.write(str.getBytes("UTF-8"));
		gzip.close();
		return outputStream.toByteArray();
	}

	/**
	 * Links an other metrics class with this class. This method is called using
	 * Reflection.
	 *
	 * @param metrics
	 *            An object of the metrics class to link.
	 */
	public static void linkMetrics(Object metrics) {
		knownMetricsInstances.add(metrics);
	}

	/**
	 * Sends the data to the bStats server.
	 *
	 * @param data
	 *            The data to send.
	 * @throws Exception
	 *             If the request failed.
	 */
	private static void sendData(JsonObject data) throws Exception {
		Validate.notNull(data, "Data cannot be null");
		HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();

		// Compress the data to save bandwidth
		byte[] compressedData = compress(data.toString());

		// Add headers
		connection.setRequestMethod("POST");
		connection.addRequestProperty("Accept", "application/json");
		connection.addRequestProperty("Connection", "close");
		connection.addRequestProperty("Content-Encoding", "gzip"); // We gzip
																	// our
																	// request
		connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
		connection.setRequestProperty("Content-Type", "application/json"); // We
																			// send
																			// our
																			// data
																			// in
																			// JSON
																			// format
		connection.setRequestProperty("User-Agent", "MC-Server/" + B_STATS_VERSION);

		// Send data
		connection.setDoOutput(true);
		DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
		outputStream.write(compressedData);
		outputStream.flush();
		outputStream.close();

		connection.getInputStream().close(); // We don't care about the response
												// - Just send our data :)
	}

	// A list with all custom charts
	private final List<CustomChart> charts = new ArrayList<>();

	// The config path
	private Path configDir;

	// Is bStats enabled on this server?
	private boolean enabled;

	// Should failed requests be logged?
	private boolean logFailedRequests = false;

	// The logger
	private Logger logger;

	// The plugin
	private final PluginContainer plugin;

	// The uuid of the server
	private String serverUUID;

	// The constructor is not meant to be called by the user himself.
	// The instance is created using Dependency Injection
	// (https://docs.spongepowered.org/master/en/plugin/injection.html)
	@Inject
	private Metrics(PluginContainer plugin, Logger logger, @ConfigDir(sharedRoot = true) Path configDir) {
		if (created) {
			// We don't want more than one instance of this class
			throw new IllegalStateException("There's already an instance of this Metrics class!");
		} else {
			created = true;
		}

		this.plugin = plugin;
		this.logger = logger;
		this.configDir = configDir;

		try {
			loadConfig();
		} catch (IOException e) {
			// Failed to load configuration
			logger.warn("Failed to load bStats config!", e);
			return;
		}

		// We are not allowed to send data about this server :(
		if (!enabled) {
			return;
		}

		Class<?> usedMetricsClass = getFirstBStatsClass();
		if (usedMetricsClass == null) {
			// Failed to get first metrics class
			return;
		}
		if (usedMetricsClass == getClass()) {
			// We are the first! :)
			linkMetrics(this);
			startSubmitting();
		} else {
			// We aren't the first so we link to the first metrics class
			try {
				usedMetricsClass.getMethod("linkMetrics", Object.class).invoke(null, this);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				if (logFailedRequests) {
					logger.warn("Failed to link to first metrics class {}!", usedMetricsClass.getName(), e);
				}
			}
		}
	}

	/**
	 * Adds a custom chart.
	 *
	 * @param chart
	 *            The chart to add.
	 */
	public void addCustomChart(CustomChart chart) {
		Validate.notNull(chart, "Chart cannot be null");
		charts.add(chart);
	}

	/**
	 * Gets the first bStat Metrics class.
	 *
	 * @return The first bStats metrics class.
	 */
	private Class<?> getFirstBStatsClass() {
		Path configPath = configDir.resolve("bStats");
		configPath.toFile().mkdirs();
		File tempFile = new File(configPath.toFile(), "temp.txt");

		try {
			String className = readFile(tempFile);
			if (className != null) {
				try {
					// Let's check if a class with the given name exists.
					return Class.forName(className);
				} catch (ClassNotFoundException ignored) {
				}
			}
			writeFile(tempFile, getClass().getName());
			return getClass();
		} catch (IOException e) {
			if (logFailedRequests) {
				logger.warn("Failed to get first bStats class!", e);
			}
			return null;
		}
	}

	/**
	 * Gets the plugin specific data. This method is called using Reflection.
	 *
	 * @return The plugin specific data.
	 */
	public JsonObject getPluginData() {
		JsonObject data = new JsonObject();

		String pluginName = plugin.getName();
		String pluginVersion = plugin.getVersion().orElse("unknown");

		data.addProperty("pluginName", pluginName);
		data.addProperty("pluginVersion", pluginVersion);

		JsonArray customCharts = new JsonArray();
		for (CustomChart customChart : charts) {
			// Add the data of the custom charts
			JsonObject chart = customChart.getRequestJsonObject(logger, logFailedRequests);
			if (chart == null) { // If the chart is null, we skip it
				continue;
			}
			customCharts.add(chart);
		}
		data.add("customCharts", customCharts);

		return data;
	}

	/**
	 * Gets the server specific data.
	 *
	 * @return The server specific data.
	 */
	private JsonObject getServerData() {
		// Minecraft specific data
		int playerAmount = Sponge.getServer().getOnlinePlayers().size();
		playerAmount = playerAmount > 200 ? 200 : playerAmount;
		int onlineMode = Sponge.getServer().getOnlineMode() ? 1 : 0;
		String minecraftVersion = Sponge.getGame().getPlatform().getMinecraftVersion().getName();

		// OS/Java specific data
		String javaVersion = System.getProperty("java.version");
		String osName = System.getProperty("os.name");
		String osArch = System.getProperty("os.arch");
		String osVersion = System.getProperty("os.version");
		int coreCount = Runtime.getRuntime().availableProcessors();

		JsonObject data = new JsonObject();

		data.addProperty("serverUUID", serverUUID);

		data.addProperty("playerAmount", playerAmount);
		data.addProperty("onlineMode", onlineMode);
		data.addProperty("minecraftVersion", minecraftVersion);

		data.addProperty("javaVersion", javaVersion);
		data.addProperty("osName", osName);
		data.addProperty("osArch", osArch);
		data.addProperty("osVersion", osVersion);
		data.addProperty("coreCount", coreCount);

		return data;
	}

	/**
	 * Loads the bStats configuration.
	 *
	 * @throws IOException
	 *             If something did not work :(
	 */
	private void loadConfig() throws IOException {
		Path configPath = configDir.resolve("bStats");
		configPath.toFile().mkdirs();
		File configFile = new File(configPath.toFile(), "config.conf");
		HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setFile(configFile).build();
		CommentedConfigurationNode node;
		if (!configFile.exists()) {
			configFile.createNewFile();
			node = configurationLoader.load();

			// Add default values
			node.getNode("enabled").setValue(true);
			// Every server gets it's unique random id.
			node.getNode("serverUuid").setValue(UUID.randomUUID().toString());
			// Should failed request be logged?
			node.getNode("logFailedRequests").setValue(false);

			// Add information about bStats
			node.getNode("enabled").setComment(
					"bStats collects some data for plugin authors like how many servers are using their plugins.\n"
							+ "To honor their work, you should not disable it.\n"
							+ "This has nearly no effect on the server performance!\n"
							+ "Check out https://bStats.org/ to learn more :)");

			configurationLoader.save(node);
		} else {
			node = configurationLoader.load();
		}

		// Load configuration
		enabled = node.getNode("enabled").getBoolean(true);
		serverUUID = node.getNode("serverUuid").getString();
		logFailedRequests = node.getNode("logFailedRequests").getBoolean(false);
	}

	/**
	 * Reads the first line of the file.
	 *
	 * @param file
	 *            The file to read. Cannot be null.
	 * @return The first line of the file or <code>null</code> if the file does not
	 *         exist or is empty.
	 * @throws IOException
	 *             If something did not work :(
	 */
	private String readFile(File file) throws IOException {
		if (!file.exists()) {
			return null;
		}
		try (FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);) {
			return bufferedReader.readLine();
		}
	}

	private void startSubmitting() {
		// We use a timer cause want to be independent from the server tps
		final Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// Plugin was disabled, e.g. because of a reload (is this even
				// possible in Sponge?)
				if (!Sponge.getPluginManager().isLoaded(plugin.getId())) {
					timer.cancel();
					return;
				}
				// The data collection (e.g. for custom graphs) is done sync
				// Don't be afraid! The connection to the bStats server is still
				// async, only the stats collection is sync ;)
				Scheduler scheduler = Sponge.getScheduler();
				Task.Builder taskBuilder = scheduler.createTaskBuilder();
				taskBuilder.execute(() -> submitData()).submit(plugin);
			}
		}, 1000 * 60 * 5, 1000 * 60 * 30);
		// Submit the data every 30 minutes, first time after 5 minutes to give
		// other plugins enough time to start
		// WARNING: Changing the frequency has no effect but your plugin WILL be
		// blocked/deleted!
		// WARNING: Just don't do it!
	}

	/**
	 * Collects the data and sends it afterwards.
	 */
	private void submitData() {
		final JsonObject data = getServerData();

		JsonArray pluginData = new JsonArray();
		// Search for all other bStats Metrics classes to get their plugin data
		for (Object metrics : knownMetricsInstances) {
			try {
				Object plugin = metrics.getClass().getMethod("getPluginData").invoke(metrics);
				if (plugin instanceof JsonObject) {
					pluginData.add((JsonObject) plugin);
				}
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
			}
		}

		data.add("plugins", pluginData);

		// Create a new thread for the connection to the bStats server
		new Thread(() -> {
			try {
				// Send the data
				sendData(data);
			} catch (Exception e) {
				// Something went wrong! :(
				if (logFailedRequests) {
					logger.warn("Could not submit plugin stats!", e);
				}
			}
		}).start();
	}

	/**
	 * Writes a String to a file. It also adds a note for the user,
	 *
	 * @param file
	 *            The file to write to. Cannot be null.
	 * @param text
	 *            The text to write.
	 * @throws IOException
	 *             If something did not work :(
	 */
	private void writeFile(File file, String text) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		try (FileWriter fileWriter = new FileWriter(file);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
			bufferedWriter.write(text);
			bufferedWriter.newLine();
			bufferedWriter.write("Note: This class only exists for internal purpose. You can ignore it :)");
		}
	}

}