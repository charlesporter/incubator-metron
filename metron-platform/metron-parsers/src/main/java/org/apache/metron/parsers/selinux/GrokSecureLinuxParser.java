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

import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;
import org.apache.metron.parsers.GrokParser;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;

public class GrokSecureLinuxParser extends GrokParser {

    private static final long serialVersionUID = 1179184051981797924L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(GrokSecureLinuxParser.class);

    private List<String> logMessageGrokLabels;
    private String logMessageField;

    @Override
    @SuppressWarnings("unchecked")
    public void configure(Map<String, Object> parserConfig) {
        logMessageGrokLabels = (List<String>) parserConfig.get("logMessageGrokLabels");
        logMessageField = (String) parserConfig.get("logMessageField");
        super.configure(parserConfig);
    }

    @Override
    protected void postParse(JSONObject message) {
        LOGGER.debug("Entered with postParse: " + message.toJSONString());
        if(message.containsKey("log_message")) {
            JSONObject logMessageJSON = parseLogMessage(message.get(logMessageField).toString());
            message.putAll(logMessageJSON);
        }

        removeEmptyFields(message);
    }

    @SuppressWarnings("unchecked")
    private void removeEmptyFields(JSONObject json) {
        LOGGER.debug("removing unnecessary fields");
        Iterator<Object> keyIter = json.keySet().iterator();
        while (keyIter.hasNext()) {
            Object key = keyIter.next();
            Object value = json.get(key);
            if (null == value || "".equals(value.toString()) ||
                    "-".equals(value.toString()) || key.toString().equals("UNWANTED") ||
                    key.toString().equals(logMessageField)) {
                keyIter.remove();
            }
        }
    }

    @Override
    protected long formatTimestamp(Object value) {
        LOGGER.debug("Formatting timestamp");
        long epochTimestamp = System.currentTimeMillis();
        if (value != null) {
            try {
                epochTimestamp = handleTimestampWithNoYear(value.toString());
            } catch (ParseException e) {
                //default to current time
                LOGGER.debug("Unable to format time correctly. Using current system time");
            }
        }
        return epochTimestamp;
    }

    private JSONObject parseLogMessage(String logMessage) {
        JSONObject returnValue = new JSONObject();

        for(String messagePatternLabel: logMessageGrokLabels) {
            try {
                String grokPattern = "%{" + messagePatternLabel + "}";
                grok.compile(grokPattern);
                Match gm = grok.match(logMessage);
                if(!gm.isNull()) {
                    gm.captures();
                    returnValue.putAll(gm.toMap());
                    removeEmptyFields(returnValue);
                    returnValue.remove(messagePatternLabel);
                    returnValue.put("message_type", messagePatternLabel);
                    break;
                }
            } catch (GrokException e) {
                LOGGER.error("Error parsing log message using pattern: " + messagePatternLabel +
                        "\n" + e.getMessage());
            }
        }
        if(!returnValue.containsKey("message_type")) {
            LOGGER.warn("Message in log line could not be parsed: " + logMessage);
        }
        try {
            grok.compile("%{" + patternLabel + "}");
        } catch (GrokException e) {
            init();
        }
        return returnValue;
    }

    /**
     * Handles timestamps that do not contain a year. This handles the case where a log is
     * written December 31st, but is parsed on January 1st (the previous year will be used).
     * @param parsedTimestamp The parsed timestamp.
     * @return A long representing <parsedTimestamp> in epoch time
     * @throws ParseException
     */
    protected long handleTimestampWithNoYear(String parsedTimestamp) throws ParseException {
        int currentYear = getCurrentYear();
        Calendar timestampCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        timestampCal.setTime(dateFormat.parse(parsedTimestamp));

        // if the log is from the previous year, decrement currentYear.
        if (isJanuaryFirst() && timestampCal.get(Calendar.MONTH) == Calendar.DECEMBER) {
            currentYear--;
        }
        timestampCal.set(Calendar.YEAR, currentYear);
        // The February 29th case is checked here because the year needs to be set prior to setting the correct date
        if (parsedTimestamp.contains("Feb 29")) {
            timestampCal.set(Calendar.MONTH, Calendar.FEBRUARY);
            timestampCal.set(Calendar.DAY_OF_MONTH, 29);
        }
        // set the year
        return timestampCal.getTimeInMillis();
    }

    /**
     * Checks whether the current day is January first or not.
     * @return True if the current day is January first; false otherwise.
     */
    private boolean isJanuaryFirst() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return calendar.get(Calendar.MONTH) == Calendar.JANUARY && calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    /**
     * Gets the current year.
     * @return The current year.
     */
    private int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }
}
