package com.mirabilia.giz.sormas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.YearMonth;

public class SormasDataProcessor {

    public static String transposeSomrasData(String jsonStrings) {

        LocalDate currentDate = LocalDate.now();
        YearMonth lastMonth = YearMonth.from(currentDate.minusMonths(1));
        LocalDate startDateOfLastMonth = lastMonth.atDay(1);
        LocalDate endDateOfLastMonth = lastMonth.atEndOfMonth();

        StringWriter sw = new StringWriter();

        // JSON string
        String jsonString = "[{\"valueSum\":23,\"disease\":\"AFP\",\"sex\":\"FEMALE\",\"deaths\":\"2\",\"cases\":\"20\"," +
                "\"districtuuid\":\"ROGBF4-UZ5Q66-Y2SMXN-FHOASMSQ\",\"agegroup\":\">40 years\"},{\"valueSum\":1,\"disease\":\"AFP\",\"sex\":\"MALE\",\"deaths\":\"2\",\"cases\":\"20\",\"districtuuid\":\"SXHSGN-RXNDZJ-XJW53Q-7BUA2K7Y\",\"agegroup\":\">40 years\"},{\"valueSum\":1,\"disease\":\"AFP\",\"sex\":\"MALE\",\"deaths\":\"2\",\"cases\":\"20\",\"districtuuid\":\"UKWW3S-4N5VHH-DSVSLH-EE2CCGYY\",\"agegroup\":\">40 years\"},{\"valueSum\":1,\"disease\":\"AFP\",\"sex\":\"MALE\",\"deaths\":\"2\",\"cases\":\"20\",\"districtuuid\":\"USQLJH-ATKAV7-HDFGQ6-KDXJCMEM\",\"agegroup\":\">40 years\"},{\"valueSum\":1,\"disease\":\"AFP\",\"sex\":\"MALE\",\"deaths\":\"2\",\"cases\":\"20\",\"districtuuid\":\"UTWDXJ-WSHS7Y-P54ICU-YUCUCL5U\",\"agegroup\":\">40 years\"},{\"valueSum\":1,\"disease\":\"AFP\",\"sex\":\"MALE\",\"deaths\":\"2\",\"cases\":\"20\",\"districtuuid\":\"XHOYN2-BPERVR-M3YCS4-BX3F2FSI\",\"agegroup\":\">40 years\"},{\"valueSum\":44,\"disease\":\"AFP\",\"sex\":\"FEMALE\",\"deaths\":\"2\",\"cases\":\"20\",\"districtuuid\":\"SUBPAP-KJLW52-FJNWHM-7Q36CD6I\",\"agegroup\":\"10-19 years\"}]";

        try {
            // Parse JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode jsonArray = (ArrayNode) objectMapper.readTree(jsonString);

            // Create XML builder

            sw.write("<dataValueSet xmlns=\"http://dhis2.org/schema/dxf/2.0\"> \n");

            // Iterate over JSON array
            for (JsonNode jsonNode : jsonArray) {
                String sex = jsonNode.get("sex").asText();
                String age_group =  jsonNode.get("agegroup").asText();

                String disease = jsonNode.get("disease").asText();
                int valueSum = jsonNode.get("valueSum").asInt();
                String dataset = "BfMAe6Itzgt";
                String organisationUnit = jsonNode.get("districtuuid").asText();
                int deaths = jsonNode.get("deaths").asInt();
                int cases = jsonNode.get("cases").asInt();
                String catComboProcessed = "";


                switch (sex) {
                    case "MALE":
                        switch (age_group) {
                            case "0-28 Days":
                                catComboProcessed = "KM2YbPwmMdz";
                                break;

                            case "29d - 11 months":
                                catComboProcessed = "kWIOCYMv1jf";
                                break;

                            case "12-59 months":
                                catComboProcessed = "JmfqYwvdxGW";
                                break;

                            case "5-9 years":
                                catComboProcessed = "Y8CkfUfJOAc";
                                break;

                            case "10-19 years":
                                catComboProcessed = "DgAeHy51LhK";
                                break;

                            case "20-40 years":
                                catComboProcessed = "EXeNdwKrWJ4";
                                break;

                            case ">40 years":
                                catComboProcessed = "HgUpVNUKFdL";
                                break;


                            default:
                                catComboProcessed = "error_from_sormas_data_Male";
                                break;
                        }
                        break;

                    case "FEMALE":
                        switch (age_group) {
                            case "0-28 Days":
                                catComboProcessed = "QDga2y16LZj";
                                break;

                            case "29d - 11 months":
                                catComboProcessed = "AmqsS5GTLCv";
                                break;

                            case "12-59 months":
                                catComboProcessed = "xIX2nXBedZU";
                                break;

                            case "5-9 years":
                                catComboProcessed = "G4tFN64PSJG";
                                break;

                            case "10-19 years":
                                catComboProcessed = "L7EdWO9sIrL";
                                break;

                            case "20-40 years":
                                catComboProcessed = "HUyfsyDv4PS";
                                break;

                            case ">40 years":
                                catComboProcessed = "s7XaqjufenV";
                                break;
                            default:
                                catComboProcessed = "error_from_sormas_data_Female";
                                break;
                        }
                        break;
                    default:
                        catComboProcessed = "error_from_sormas_data";
                        break;
                }





                sw.write("  <dataValue categoryOptionCombo=\"" + catComboProcessed + "\" dataElement=\"" + disease + "\" value=\"" + valueSum + "\" dataSet=\"dataset\" period=\"" + lastMonth.toString().replace("-", "") + "\" orgUnit=\""+DistrictMapFile.getLeftColumnValue(organisationUnit)+"\"/>\n");
            }

            sw.write("</dataValueSet>");

            // Print XML

            System.out.println(sw.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sw.toString();
    }











    private static void checkDates(){

        LocalDate currentDate = LocalDate.now();
        YearMonth lastMonth = YearMonth.from(currentDate.minusMonths(1));
        LocalDate startDateOfLastMonth = lastMonth.atDay(1);
        LocalDate endDateOfLastMonth = lastMonth.atEndOfMonth();
        System.out.println("Start date of last month: " + startDateOfLastMonth);
        System.out.println("End date of last month: " + endDateOfLastMonth);

    }
}
