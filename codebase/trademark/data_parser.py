import logging

from bs4 import BeautifulSoup


def parse_application_data(response: str) -> dict | None:
    try:

        table_data = {}
        soup = BeautifulSoup(response, 'html.parser')

        tables = soup.select('#panelgetdetail table')

        status_table = tables[1]
        status_td = status_table.find_all('tr')

        status = status_td[1].find('font', color='red').text
        table_data.update({'status': status})

        target_table = None
        try:
            if len(tables[2].find_all('tr')) > 4:
                target_table = tables[2]
            else:
                target_table = tables[3]
        except IndexError:
            logging.error("Error while finding right table")

        rows = target_table.select('tr')

        try:
            for row in rows:
                cells = row.find_all('td')
                if len(cells) == 2:
                    key = cells[0].get_text(strip=True)
                    value = cells[1].get_text(" ", strip=True)
                    table_data[key] = value
        except IndexError:
            logging.error("Error while parsing table")

        return table_data

    except Exception as e:
        logging.error(f"Error while parsing data: {e}")
        return None


def parse_payload(response: str) -> dict | None:
    try:
        soup = BeautifulSoup(response, 'html.parser')

        event_target = 'SearchWMDatagrid$ctl03$lnkbtnappNumber1'
        hidden_field = (soup.find(id='ToolkitScriptManager1_HiddenField').get("value", "")) \
            if soup.find(id='ToolkitScriptManager1_HiddenField') else ""
        event_argument = (soup.find(id='__EVENTARGUMENT').get("value", ""))\
            if soup.find(id='__EVENTARGUMENT') else ""
        view_state = (soup.find(id='__VIEWSTATE').get("value", ""))\
            if soup.find(id='__VIEWSTATE') else ""
        view_state_generator = (soup.find(id='__VIEWSTATEGENERATOR').get("value", ""))\
            if soup.find(id='__VIEWSTATEGENERATOR') else ""
        view_state_encrypted = (soup.find(id='__VIEWSTATEENCRYPTED').get("value", ""))\
            if soup.find(id='__VIEWSTATEENCRYPTED') else ""
        event_validation = (soup.find(id='__EVENTVALIDATION').get("value", ""))\
            if soup.find(id='__EVENTVALIDATION') else ""

        data = {
            "ToolkitScriptManager1_HiddenField": hidden_field,
            "__EVENTTARGET": event_target,
            "__EVENTARGUMENT": event_argument,
            "__VIEWSTATE": view_state,
            "__VIEWSTATEGENERATOR": view_state_generator,
            "__VIEWSTATEENCRYPTED": view_state_encrypted,
            "__EVENTVALIDATION": event_validation,
        }

        return data
    except Exception as e:
        logging.error(f"Error while parsing payload: {e}")
        return None
