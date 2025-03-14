package com.ark

import org.jsoup.Jsoup
import org.jsoup.nodes.Document


data class Trademark(
    val tmId: Long? = null,
    val status: String,
    val applicationNumber: String,
    val tmClass: String,
    val dateOfApplication: String,
    val appropriateOffice: String?,
    val state: String?,
    val country: String?,
    val filingMode: String?,
    val tmAppliedFor: String,
    val tmCategory: String?,
    val tmType: String,
    val userDetails: String?,
    val certDetail: String?,
    val validUpTo: String?,
    val proprietorName: String?,
    val proprietorAddress: String?,
    val emailId: String?,
    val agentName: String?,
    val agentAddress: String?,
    val publicationDetails: String?
)

fun main() {
    val html = """
        <div id="panelgetdetail">
        	
                    <span id="lblappdetail"><table border="0" width="680px" align="Center" cellspacing="0" style="font-size=larger; "><tbody><tr valign="top"><td align="center"><font color="Red"><b>(NOT FOR LEGAL USE)</b></font></td></tr></tbody></table><table border="0" width="680px" align="Center" cellspacing="0" style="font-size=larger; "><tbody><tr valign="top"><td align="Left" width="250px;"><font color="navy"><b>As on Date : </b></font><b><font color="red">14/03/2025</font></b></td><td align="Right">&nbsp;</td></tr><tr valign="top"><td align="Left"><font color="navy"><b>Status &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; : </b></font><font color="red"><b>Formalities Chk Pass</b></font></td><td align="Right">&nbsp;</td></tr></tbody></table><table border="1" width="680px" align="Center" cellspacing="0" style="font-size=larger; background-color:mintcream;"><tbody><tr valign="top"><td width="200px" bgcolor="lightsteelblue">TM Application No.</td><td width="480px">6870229</td></tr><tr valign="top"><td bgcolor="lightsteelblue">Class </td><td>43</td></tr><tr valign="top"><td bgcolor="lightsteelblue">Date of Application</td><td>21/02/2025</td></tr><tr valign="top"><td bgcolor="lightsteelblue">Appropriate Office</td><td>DELHI</td></tr><tr valign="top"><td bgcolor="lightsteelblue">State</td><td>PUNJAB</td></tr><tr valign="top"><td bgcolor="lightsteelblue">Country</td><td>India</td></tr><tr valign="top"><td bgcolor="lightsteelblue">Filing Mode </td><td>e-Filing</td></tr><tr valign="top"><td bgcolor="lightsteelblue">TM Applied For</td><td>CHAA PAANI</td></tr><tr valign="top"><td bgcolor="lightsteelblue">TM Category </td><td>TRADE MARK</td></tr><tr valign="top"><td bgcolor="lightsteelblue">Trade Mark Type</td><td>DEVICE</td></tr><tr valign="top"><td bgcolor="lightsteelblue">User Detail </td><td>02/02/2024</td></tr><tr valign="top"><td bgcolor="lightsteelblue">Certificate Detail</td><td></td></tr><tr valign="top"><td bgcolor="lightsteelblue">Valid upto/ Renewed upto</td><td></td></tr><tr valign="top"><td bgcolor="lightsteelblue">Proprietor name</td><td>(1) IXC HOSPITALITY<br>Partnership Firm Details : Gurdeep Singh Arora, Aikjot Singh Sandhu</td></tr><tr valign="top"><td bgcolor="lightsteelblue">Proprietor Address </td><td>SHOP NO. 87, COMMERCIAL POCKET-2, SECTOR 66-A, MOHALI, S.A.S. NAGAR, PUNJAB-160062</td></tr><tr valign="top"><td bgcolor="lightsteelblue">Email Id </td><td>****emarks@safe****eapprovals.com</td></tr><tr valign="top"><td bgcolor="lightsteelblue">Attorney name</td><td>RAMANJIT KAUR[46720]</td></tr><tr valign="top"><td bgcolor="lightsteelblue">Attorney Address </td><td>#5440, Block-F, Block-F, Aerocity, S.A.S. Nagar, Punjab-140306</td></tr><tr valign="top"><td bgcolor="lightsteelblue"> Goods &amp; Service Details</td><td>[CLASS : 43] <br>Services for providing food and drink; cafes; tea bars; coffee shops; snack bars; restaurants; self-service restaurants; food kiosks; mobile food and drink catering; tea and coffee house services; preparation and provision of beverages; take-away food and drink services; juice bars; ice cream parlors; catering services; providing temporary accommodation.</td></tr></tbody></table><table align="center" border="1" cellspacing="0"><tbody><tr><td align="center"><font size="3" color="maroon">Trade Mark Image : (1) </font></td></tr><tr><td align="center"><img src="imagedoc.aspx?ID=1&amp;APPNUMBER=WFlaW1xdLS8uJykpMFhZWltcXQ=="></td></tr></tbody></table></span>
                    <div id="printedView">
                        <br><table align="center" border="0">
                            
                            <tbody><tr>
                                <td align="center">
                                    <input type="submit" name="btnprint_detail" value="PRINT" onclick="show(false,'printedView');print();show(true,'printedView');" id="btnprint_detail" style="color:Blue;font-size:10pt;font-weight:bold;">
                                </td>

                            </tr>
                        </tbody></table>
                        <br><table align="center" border="0">
                            

                            <tbody><tr>
                                

                                <td align="Left">
                                    <input type="submit" name="btnpr" value="PR Details" id="btnpr" style="color:Blue;font-size:10pt;font-weight:bold;">
                                </td>
                                 <td align="Right">
                                    <input type="submit" name="Button1" value="Reminders" onclick="javascript:WebForm_DoPostBackWithOptions(new WebForm_PostBackOptions(&quot;Button1&quot;, &quot;&quot;, true, &quot;&quot;, &quot;&quot;, false, false))" id="Button1" style="color:Blue;font-size:10pt;font-weight:bold;">
                                </td>
                                <td align="Right">
                                    <input type="submit" name="btnNotice" value="Correspondence &amp; Notices" onclick="javascript:WebForm_DoPostBackWithOptions(new WebForm_PostBackOptions(&quot;btnNotice&quot;, &quot;&quot;, true, &quot;&quot;, &quot;&quot;, false, false))" id="btnNotice" style="color:Blue;font-size:10pt;font-weight:bold;">
                                </td>
                                <td align="Left">
                                    <input type="submit" name="btndocument" value="Uploaded Documents" id="btndocument" style="color:Blue;font-size:10pt;font-weight:bold;">
                                </td>
                                <td>
                                    <input type="submit" name="btnExit" value="EXIT" onclick="javascript:WebForm_DoPostBackWithOptions(new WebForm_PostBackOptions(&quot;btnExit&quot;, &quot;&quot;, true, &quot;&quot;, &quot;&quot;, false, false))" id="btnExit" style="color:Green;font-size:10pt;font-weight:bold;">
                                </td>

                            </tr>
                        </tbody></table>

                        <table align="center" border="0" style="width: 725px;">
                            <tbody><tr>
                                <td align="left">
                                    <p align="justify">
                                        </p><h6><font color="Red">WARNING/DISCLAIMER</font>:  THE DATA OF TRADE MARKS REGISTRY IS UNDER THE PROCESS OF DIGITISATION, IF ANY DISCREPANCY IS OBSERVED IN THE DATA PLEASE CONTACT OR SUBMIT AT APPROPRIATE TRADE MARKS REGISTRY ALONGWITH SUPPORTING DOCUMENTS. THIS WILL HELP IN UPDATION OF ELECTRONIC RECORDS.</h6>
                                    <p></p>
                                </td>
                            </tr>
                        </tbody></table>
                    </div>
        </div>
    """.trimIndent()
    val doc: Document = Jsoup.parse(html)

    val dataMap = mutableMapOf<String, String>()

    val statusElement = doc.select("td font:contains(Status)").first()?.nextElementSibling()
    val status = statusElement?.text()?.trim() ?: "Status not found"
    dataMap["Status"] = status
    println(status)


    // Extract key-value pairs from the main table
    val rows = doc.select("table[border='1'] tr")
    for (row in rows) {
        val cells = row.select("td")
        if (cells.size == 2) {
            val key = cells[0].text().trim()
            val value = cells[1].html().trim().replace("\n", ", ") // Keep text formatting
            dataMap[key] = value
        }
    }

    Trademark(
        applicationNumber = dataMap["TM Application No."] ?: throw Exception("No Application Number found"),
        status = dataMap["Status"] ?: throw Exception("No Status found"),
        tmClass = dataMap["Class"] ?: throw Exception("No Class Found"),
        dateOfApplication = dataMap["Date of Application"] ?: throw Exception("No Date of Application found"),
        appropriateOffice = dataMap["Appropriate Office"],
        state = dataMap["State"],
        country = dataMap["Country"],
        filingMode = dataMap["Filing Mode"],
        tmAppliedFor = dataMap["TM Applied For"] ?: throw Exception("No TM Applied For found"),
        tmCategory = dataMap["TM Category"],
        tmType = dataMap["Trade Mark Type"] ?: throw Exception("No Trade Mark Type found"),
        userDetails = dataMap["User Detail"],
        certDetail = dataMap["Certificate Detail"],
        validUpTo = dataMap["Valid upto/ Renewed upto"],
        proprietorName = dataMap["Proprietor name"],
        proprietorAddress = dataMap["Proprietor Address"],
        emailId = dataMap["Email Id"],
        agentName = dataMap["Attorney name"],
        agentAddress = dataMap["Attorney Address"],
        publicationDetails = dataMap["Publication detail"]
    )
    dataMap.forEach { entity ->
        println("${entity.key} : ${entity.value}")
    }

}