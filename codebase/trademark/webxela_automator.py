import logging
import os
from concurrent.futures.thread import ThreadPoolExecutor
from math import ceil
import requests

from doc_utils.spreadsheet_utils import combine_excel_files, save_to_excel
from trademark.captcha_requester import request_captcha
from trademark.data_parser import parse_application_data
from trademark.trademark_requester import request_trademark_data

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

EXTRACTED_DATA_DIR = "data/extracted"


def automate_webxela(threads: int):
    pdf_data = "data/application_num/application_numbers.txt"
    generated_files = []


    initial_cleanup(EXTRACTED_DATA_DIR)

    try:
        with open(pdf_data, 'r') as file:
            application_numbers = file.readlines()
            application_numbers = [num.strip() for num in application_numbers]
            chunk_size = ceil(len(application_numbers) / threads)

            chunks = [application_numbers[i:i + chunk_size] for i in range(0, len(application_numbers), chunk_size)]

            with ThreadPoolExecutor(max_workers=threads) as executor:
                futures = [executor.submit(process_chunk, chunk, idx + 1, generated_files) for idx, chunk in
                           enumerate(chunks)]
                for future in futures:
                    future.result()

        combine_excel_files()

    except Exception as e:
        logging.error(f"Error during processing: {e}")
        print("Error occurred. Cleaning up generated files...")

        cleanup_generated_files(generated_files)

    finally:
        print("Process finished.")


def process_chunk(chunk, thread_id, generated_files):

    session = requests.Session()
    session.get(
        url="https://tmrsearch.ipindia.gov.in/eregister/captcha.ashx",
        verify=False
    )
    session.get(
        url="https://tmrsearch.ipindia.gov.in/eregister/Application_View.aspx",
        verify=False
    )

    captcha = request_captcha(session)

    filename = f'{EXTRACTED_DATA_DIR}/trademark_data_thread_{thread_id}.xlsx'

    try:
        if captcha:
            for app_num in chunk:
                response = request_trademark_data(
                    appl_number=app_num,
                    captcha_value=captcha,
                    session=session,
                )
                trademark_data = parse_application_data(response)

                if trademark_data:
                    save_to_excel(trademark_data, filename)
                else:
                    logging.error(f"Error parsing trademark data for application number: {app_num}")
            generated_files.append(filename)
        else:
            logging.error(f"Error retrieving captcha in thread {thread_id}")
            raise Exception("Captcha retrieval failed")

    except Exception as e:
        logging.error(f"Error during data extraction in thread {thread_id}: {e}")
        if os.path.exists(filename):
            os.remove(filename)
        raise


def cleanup_generated_files(generated_files):
    """Delete only generated Excel files that start with 'trademark_data_thread'."""
    for file in generated_files:
        if os.path.exists(file) and os.path.basename(file).startswith("trademark_data_thread"):
            try:
                os.remove(file)
                logging.info(f"Deleted file: {file}")
            except Exception as e:
                logging.error(f"Failed to delete file {file}: {e}")


def initial_cleanup(directory):
    """Delete only Excel files that start with 'trademark_data_thread' in the given directory."""
    try:
        for filename in os.listdir(directory):
            if filename.startswith("trademark_data_thread") and filename.endswith(".xlsx"):
                file_path = os.path.join(directory, filename)
                os.remove(file_path)
                logging.info(f"Deleted existing file: {file_path}")
    except Exception as e:
        logging.error(f"Failed during initial cleanup: {e}")
