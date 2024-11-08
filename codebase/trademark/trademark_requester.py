import logging
from requests import Session

from trademark.data_parser import parse_payload

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def request_trademark_data(
        appl_number: str,
        captcha_value,
        session: Session,
) -> str | None:
    url = 'https://tmrsearch.ipindia.gov.in/eregister/Application_View.aspx'

    initial_payload = {
        "ToolkitScriptManager1_HiddenField": ";;AjaxControlToolkit, Version=3.5.11119.20050, Culture=neutral, PublicKeyToken=28f01b0e84b6d53e:en-US:8e147239-dd05-47b0-8fb3-f743a139f982:865923e8:91bd373d:8e72a662:411fea1c:acd642d2:596d588c:77c58d20:14b56adc:269a19ae",
        "__EVENTTARGET": "",
        "__EVENTARGUMENT": "",
        "__VIEWSTATE": "k7HwUpD+/u6oJ2DqKOrJ39STGAFzFG9W96QkaPkm2/moFhhnROgF2+90jxZD5rWZsQpTL+yjza+bNQw1qu/GIYDZqyhrcztNQRcLxUjMXUHPBbI4biBkya7uO/piLcgCWsC69FLoKZazerao85AH/Pri67tMqBL9hGEAWtEZJGwxfONVG8t89Iy4f/GeDpDtn/S9qHISBtU7+xuJt2zfyifZZyX7ohaecruBvDMroPfu7qScmZNSI5ZUInQMhKMt4q75MHJXAVawfA2CyIdwR7nwI8YGmrBB0mXosYdYnUzUhVXBCG0Glhxgzz5NSio7gBt30OiyCAgZkfmWjV4Z4b2v9hHllbb2axtMi9eUCXmAL9jOTfRDBlzJfxHuxm9nxx8uiPVqA3v221RzQz7e2J+MiPuaholKDWhHIwf7v091wgqG+hPJ/UG9MvwTZQ9O8BBPlE0KNZ/TKkeMFF6lOurdcVTliATNslQpwZMnhKCQj2oKyepW1rqwHq9dgReBc4tqJzS3M0tt0JelN40DSBNgjnPjWOZIao9I7JEoKgqo0JmSsBH4gXO9sqjnnO/skFGi3Ag4yakqCAhCicb45iraRIgHi10TV57TPzb3CFrUf7LmaUx3IR8NaQrrvknMmbz/U3l926+Wdeyo5hGFW/rtiQorxavgpoCLwYxP48NiFhJsy8LWGUjPzl+lOr2A9tt7fLf97ouvCxC0yVLhQXorDLWvxebGXsv/BkRsA9/cjTRDEOOCq+mCagsuxHu/oJ3z7akxivDYhv0An/L28Qq6jluO2TWAY0OW0ArCUPch9Unf1/E5zxsW+Ka3XCIB9uvbPvInZH3bBjBvGP6pE1bxdL/p9KXdENhREeFDyW4uTo2JnWqFx7ocVFZCShgnEQ1mzTedMSQwnh3OI4NEoHpW05cuf2kEiVxLbLotlnac14BW0BZpGpx+kvJDvwE+UaJeoFQcVwvS+Wew2NzCqWmAgyEmChyjRF3pNkSJhgSNPw7e51G5OWgE4nLcJZ8+jbHju+6Gk4WUbe3oFe/JwEoE02SSXk9Pk/7EUe8Hg0U=",
        "__VIEWSTATEGENERATOR": "B8CF52B9",
        "__VIEWSTATEENCRYPTED": "",
        "__EVENTVALIDATION": "nicoYHccqo6DQtPebbyJ/n338HXj1XAZNl88RioD15bdqA7IbTP8R4JZ7oYKChTQiAizU+UsLF0qsrIJgAZKRrWrFc7zXtvzrLI+6OHOSZeJJQewEV4RUr5oTQpEJ7Jfowe1g0IPHuPD8G6rWuaaBfWv4Jn3B1h99J6QsNuzcof529eccaNHjFsxya/aFs6nqT9WWFKvZPGCz7iKZa60UwYBO613aFsCLFEHW2L4/i6lofXd1CVAt7RrG+ZhzjlsVsGei+T6pcznVyJm8l6ZNg==",
        "applNumber": appl_number,
        "captcha1": captcha_value,
        "btnView": "View"
    }


    # basic trademark info
    initial_response = session.post(
        url=url,
        data=initial_payload,
        verify=False
    )

    # getting detailed trademark info
    final_payload = parse_payload(initial_response.text)
    response = session.post(
        url=url,
        data=final_payload,
        verify=False
    )

    if response.status_code == 200:
        logging.info("Trademark Data Request was successful!")
        # print(response.text)
        return response.text
    else:
        logging.error("Failed with status code:", response.status_code)
        logging.error("Response text:", response.text)
        return None
