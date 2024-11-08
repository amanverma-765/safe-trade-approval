import logging

from requests import Session


def request_captcha(session: Session) -> str | None:

    url = 'https://tmrsearch.ipindia.gov.in/eregister/Viewdetails_Copyright.aspx/GetCaptcha'

    payload = {}

    response = session.post(url = url, json=payload, verify=False)

    if response.status_code == 200:
        logging.info("Captcha Request was successful!")
        logging.info(f"Response captcha: {response.json().get('d')}")
        return response.json().get('d')
    else:
        logging.error(f"Failed with status code: {response.status_code}")
        logging.error(f"Response text: {response.text}")
        return None

