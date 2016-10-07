/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.parsers.selinux;

import org.apache.metron.parsers.AbstractConfigTest;
import org.apache.metron.parsers.asa.GrokAsaParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;

import java.util.*;


/**
 * <ul>
 * <li>Title: </li>
 * <li>Description: </li>
 * <li>Created: Jul 14, 2016 by: </li>
 * </ul>
 * @author $Author:  $
 * @version $Revision: 1.1 $
 */
public class GrokSecureLinuxParserTest extends AbstractConfigTest{
     /**
     * The grokSecureLinuxStrings.
     */
    private static String[] grokSecureLinuxStrings=null;

     /**
     * The grokSecureLinuxParser.
     */

    private GrokSecureLinuxParser grokSecureLinuxParser=null;

     /**
     * Constructs a new <code>GrokSecureLinuxParserTest</code> instance.
     * @throws Exception
     */

    public GrokSecureLinuxParserTest() throws Exception {
          super();

    }
	/**
	 * @throws Exception
	 */
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws Exception
	 */
	public static void tearDownAfterClass() throws Exception {
		setGrokSecureLinuxStrings(null);
	}

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
	public void setUp() throws Exception {
          super.setUp("org.apache.metron.parsers.selinux.GrokSecureLinuxParserTest");
          setGrokSecureLinuxStrings(super.readTestDataFromFile(this.getConfig().getString("logFile")));
          grokSecureLinuxParser = new GrokSecureLinuxParser();
	}

    /**
     *
     *
     * @throws Exception
     */
    public void tearDown() throws Exception {
        grokSecureLinuxParser = null;
    }

    /**
     * Test method for {@link org.apache.metron.parsers.selinux.GrokSecureLinuxParser#parse(byte[])}.
     */
    @SuppressWarnings({ "rawtypes" })
    public void testParse() {

        for (String grokSecureLinuxString : getGrokSecureLinuxStrings()) {
            JSONObject parsed = grokSecureLinuxParser.parse(grokSecureLinuxString.getBytes()).get(0);
            Assert.assertNotNull(parsed);

            System.out.println(parsed);
            JSONParser parser = new JSONParser();

            Map json=null;
            try {
                json = (Map) parser.parse(parsed.toJSONString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //Ensure JSON returned is not null/empty
            Assert.assertNotNull(json);

            Iterator iter = json.entrySet().iterator();


            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Assert.assertNotNull(entry);

                String key = (String) entry.getKey();
                Assert.assertNotNull(key);

//                String value = (String) json.get("CISCO_TAGGED_SYSLOG").toString();
//                Assert.assertNotNull(value);
            }
        }
    }

    /**
     * Returns GrokSecureLinux Input String
     */
    public static String[] getGrokSecureLinuxStrings() {
        return grokSecureLinuxStrings;
    }


    /**
     * Sets GrokSecureLinux Input String
     */
    public static void setGrokSecureLinuxStrings(String[] strings) {
        GrokSecureLinuxParserTest.grokSecureLinuxStrings = strings;
    }

    /**
     * Returns the grokSecureLinuxParser.
     * @return the grokSecureLinuxParser.
     */

    public GrokSecureLinuxParser getGrokSecureLinuxParser() {
        return grokSecureLinuxParser;
    }


    /**
     * Sets the grokSecureLinuxParser.
     * @param grokSecureLinuxParser the grokSecureLinuxParser.
     */

    public void setGrokSecureLinuxParser(GrokSecureLinuxParser grokSecureLinuxParser) {

        this.grokSecureLinuxParser = grokSecureLinuxParser;
    }

    public static void main(String[] args) {
        try {
            GrokSecureLinuxParserTest test = new GrokSecureLinuxParserTest();
            test.setUp();
            test.getGrokSecureLinuxParser().configure(getConfigs());
            test.testParse();
            test.tearDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Object> getConfigs() {
        HashMap<String, Object> returnValue = new HashMap<>();
        returnValue.put("grokPath","/patterns/securelinux");
        returnValue.put("patternLabel","SECUREBASE");
        returnValue.put("timestampField","timestamp_string");
        returnValue.put("dateFormat","MMM dd HH:mm:ss");
        List<String> list = Arrays.asList("SECURESUDORUN","SECURESSHDREST","SECURESSHDREST1","SECURESSHDREST2","SECURESSHDREST3","SECURESSHDREST4","SECURENETREG","SECURENETBADGE","AUTHFAILURE","USERMESSAGE","AUTHENTICATION_SUCCEEDED_PASSWORDLESS","AUTHENTICATION_SUCCEEDED","AUTHENTICATION_FAILED","FAILEDPASSWORD");
        returnValue.put("logMessageGrokLabels", list);
        returnValue.put("logMessageField", "log_message");
        return returnValue;
    }
}
