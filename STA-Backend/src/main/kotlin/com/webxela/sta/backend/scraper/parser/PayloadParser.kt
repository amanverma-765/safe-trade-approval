package com.webxela.sta.backend.scraper.parser

import io.ktor.client.request.forms.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class PayloadParser {

    private val logger = LoggerFactory.getLogger(PayloadParser::class.java)

    fun parsePayloadToFormData(response: String): MultiPartFormDataContent? {

        return try {
            val doc: Document = Jsoup.parse(response)

            val eventTarget = "SearchWMDatagrid\$ctl03\$lnkbtnappNumber1"
            val hiddenField = doc.getElementById("ToolkitScriptManager1_HiddenField")?.attr("value") ?: ""
            val eventArgument = doc.getElementById("__EVENTARGUMENT")?.attr("value") ?: ""
            val viewState = doc.getElementById("__VIEWSTATE")?.attr("value") ?: ""
            val viewStateGenerator = doc.getElementById("__VIEWSTATEGENERATOR")?.attr("value") ?: ""
            val viewStateEncrypted = doc.getElementById("__VIEWSTATEENCRYPTED")?.attr("value") ?: ""
            val eventValidation = doc.getElementById("__EVENTVALIDATION")?.attr("value") ?: ""

            // Create FormData
            formData {
                append("ToolkitScriptManager1_HiddenField", hiddenField)
                append("__EVENTTARGET", eventTarget)
                append("__EVENTARGUMENT", eventArgument)
                append("__VIEWSTATE", viewState)
                append("__VIEWSTATEGENERATOR", viewStateGenerator)
                append("__VIEWSTATEENCRYPTED", viewStateEncrypted)
                append("__EVENTVALIDATION", eventValidation)
            }.let { MultiPartFormDataContent(it) }
        } catch (e: Exception) {
            logger.error("Error while parsing payload: ${e.message}")
            null
        }
    }

    fun getStaticFormData(appId: String, captcha: String): MultiPartFormDataContent {
        return MultiPartFormDataContent(
            formData {
                append(
                    "ToolkitScriptManager1_HiddenField",
                    ";;AjaxControlToolkit, Version=3.5.11119.20050, Culture=neutral, PublicKeyToken=28f01b0e84b6d53e:en-US:8e147239-dd05-47b0-8fb3-f743a139f982:865923e8:91bd373d:8e72a662:411fea1c:acd642d2:596d588c:77c58d20:14b56adc:269a19ae"
                )
                append("__EVENTTARGET", "")
                append("__EVENTARGUMENT", "")
                append(
                    "__VIEWSTATE",
                    "d22D2yJNJ54owrC4KtX7FNLk6H3j/sWdhvjmv2FaCp2LbKr3XyHJIjegKTMaJAlb2XuI86Q2j5XdxjScegzIYzVeYBIy6frmGDAsnSI8cGqlsxnM3cwB69RFyVnZ5DagjU8X3/xzCyxM9CqygsBN0EgwidoQUE3HLYIElZlbYvwIkg8/tDzjVnWmeUA5J0JQEgsNNJ25ib/x6uZwLA00D/LyvI64fQ82YqgWpRYX5viFg4OcdQKYJFabEjvcq0xxOPeLCRKsAYbHiTBG5Z6zbD2R0l8Z++Ch33aynikEQmy4ab1pGklmrzJLkxeAAw7E2mLSvQLFYDhRhAj69T0K9whDnbOpTn2/yd+gNlKvwE2x4GXkhTa6+mdQD01c+VR8dhLtCKKl6VN9tTM+Wwb2FIf/epVakHcdNpuUCcWgwMk73ZJPbEylxEyh7rARjqRj6tw5Utd1RrE+Hi4+GsQG3/RNrEAq+rbVyM2+w3WBTLk5L4TGhoabNRshBdGFVpp6PXGqxBco7VcNQQfCvZwdx/o8hUvpx+btlHqEbyXjMaC0ZXl5kXysGRk9gyo/8v89vlonJBrw7uzYZPlah6f3KgmwAV1rV/KvqOUxid3ygJ8JZur2SvnV+oNYZrDxfMxc"
                )
                append("__VIEWSTATEGENERATOR", "B8CF52B9")
                append("__VIEWSTATEENCRYPTED", "")
                append(
                    "__EVENTVALIDATION",
                    "bbMB2+Q6+1l/ei7Pn4mm95fLLX4Agi0b/LpALnOFso2O+GMBAfpDTBetyE3F0bc41GMnBUOTDZAnGJ2l4XULua5yhHJIwSpRu8+Pu3LPb4bsmYpFIHgyRCEn7AMkH4FtxDe6Xsd9X35vRtFYAC6KPyT1aV8QggSjRz2vwi5nmPz84IVJA9Ku6qycHJZ9gGvm5ysP6g9Qp1QH2brGzERzUCQyBSzXvOBRTEFbXsSdfmuOCEKQMGcYemgsN/3q9jBHTl8mqGa2Jg3Y1IRV2TD0pA=="
                )
                append("applNumber", appId)
                append("captcha1", captcha)
                append("btnView", "View")
            }
        )
    }
}