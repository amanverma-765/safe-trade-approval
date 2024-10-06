import logging
import re

from PyPDF2 import PdfReader


def extract_pdf(pdf_path: str) -> bool:
    try:
        logging.info("Extracting the PDF document...")
        pdf_reader = PdfReader(pdf_path)
        extracted_text = []

        for page_num in range(len(pdf_reader.pages)):
            page = pdf_reader.pages[page_num]
            text = page.extract_text()
            extracted_text.append(text)

        regex = r"\d{7}"
        pattern = re.compile(regex)

        output_file = "data/application_num/application_numbers.txt"
        with open(output_file, "w") as file:
            for match in pattern.findall("".join(extracted_text)):
                file.write(match + "\n")

        return True

    except Exception as e:
        logging.error(f"Error while extracting pdf: {e}")
        return False