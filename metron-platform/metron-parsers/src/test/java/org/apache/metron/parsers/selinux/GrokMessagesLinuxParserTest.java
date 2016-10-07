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
public class GrokMessagesLinuxParserTest extends AbstractConfigTest{
     /**
     * The grokMessagesLinuxStrings.
     */
    private static String[] grokMessagesLinuxStrings=null;

     /**
     * The grokMessagesLinuxParser.
     */

    private GrokSecureLinuxParser grokMessagesLinuxParser=null;

     /**
     * Constructs a new <code>GrokMessagesLinuxParserTest</code> instance.
     * @throws Exception
     */

    public GrokMessagesLinuxParserTest() throws Exception {
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
		setGrokMessagesLinuxStrings(null);
	}

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
	public void setUp() throws Exception {
          super.setUp("org.apache.metron.parsers.selinux.GrokMessagesLinuxParserTest");
          setGrokMessagesLinuxStrings(super.readTestDataFromFile(this.getConfig().getString("logFile")));
          grokMessagesLinuxParser = new GrokSecureLinuxParser();
	}

    /**
     *
     *
     * @throws Exception
     */
    public void tearDown() throws Exception {
        grokMessagesLinuxParser = null;
    }

    /**
     * Test method for {@link GrokSecureLinuxParser#parse(byte[])}.
     */
    @SuppressWarnings({ "rawtypes" })
    public void testParse() {

        for (String grokMessagesLinuxString : getGrokMessagesLinuxStrings()) {
            JSONObject parsed = grokMessagesLinuxParser.parse(grokMessagesLinuxString.getBytes()).get(0);
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
     * Returns GrokMessagesLinux Input String
     */
    public static String[] getGrokMessagesLinuxStrings() {
        return grokMessagesLinuxStrings;
    }


    /**
     * Sets GrokMessagesLinux Input String
     */
    public static void setGrokMessagesLinuxStrings(String[] strings) {
        GrokMessagesLinuxParserTest.grokMessagesLinuxStrings = strings;
    }

    /**
     * Returns the grokMessagesLinuxParser.
     * @return the grokMessagesLinuxParser.
     */

    public GrokSecureLinuxParser getGrokMessagesLinuxParser() {
        return grokMessagesLinuxParser;
    }


    /**
     * Sets the grokMessagesLinuxParser.
     * @param grokMessagesLinuxParser the grokMessagesLinuxParser.
     */

    public void setGrokMessagesLinuxParser(GrokSecureLinuxParser grokMessagesLinuxParser) {

        this.grokMessagesLinuxParser = grokMessagesLinuxParser;
    }

    public static void main(String[] args) {
        try {
            GrokMessagesLinuxParserTest test = new GrokMessagesLinuxParserTest();
            test.setUp();
            test.getGrokMessagesLinuxParser().configure(getConfigs());
            test.testParse();
            test.tearDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Object> getConfigs() {
        HashMap<String, Object> returnValue = new HashMap<>();
        returnValue.put("grokPath","/patterns/messageslinux");
        returnValue.put("patternLabel","SECUREBASE");
        returnValue.put("timestampField","timestamp_string");
        returnValue.put("dateFormat","MMM dd HH:mm:ss");
        List<String> list = Arrays.asList("MESSAGE1","MESSAGE2","MESSAGE3","MESSAGE4","MESSAGE5","STATECHANGE","MESSAGE6","ACTIVATIONFAILED","POLICYSET","LISTENING");
        returnValue.put("logMessageGrokLabels", list);
        returnValue.put("logMessageField", "log_message");
        return returnValue;
    }
}
