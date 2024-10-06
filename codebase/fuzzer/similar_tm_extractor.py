import pandas as pd
import re
import jellyfish
from fuzzywuzzy import fuzz
import logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')


def trademark_spelling_similarity(trademark1, trademark2):
    similarity_ratio = fuzz.ratio(trademark1.lower(), trademark2.lower())
    return similarity_ratio


def clean_trademark(trademark, switch):
    if switch == 1:
        return re.sub(r'[^a-zA-Z\s]', '', trademark)
    else:
        return re.sub(r'[^a-zA-Z]', '', trademark)


def get_metaphone_code(word):
    return jellyfish.metaphone(word)


def split_trademark_into_substrings(trademark):
    words = []
    length = len(trademark)

    for i in range(length):
        for j in range(i + 4, length + 1):
            words.append(trademark[i:j])
    return words


def phonetic_substring_match(trademark1, trademark2):
    matched_words = []

    cleaned_user_tm = clean_trademark(trademark1, 1)
    cleaned_our_tm = clean_trademark(trademark2, 0)

    trademark1_substrings = cleaned_user_tm.split()
    trademark2_substrings = split_trademark_into_substrings(cleaned_our_tm)

    for myword in trademark1_substrings:
        client_code = get_metaphone_code(myword)

        for theirword in trademark2_substrings:
            their_code = get_metaphone_code(theirword)

            if client_code == their_code:
                if trademark_spelling_similarity(myword, theirword) > 73:
                    matched_words.append("'{}' matched with '{}'".format(myword, theirword))
    return matched_words


def extract_similar_tm(
        user_sheet: str,
        trademark_sheet: str,
        output_sheet: str = 'data/extracted/sim.xlsx',
):
    similarity_threshold: int = 80
    save_interval: int = 10

    logger = logging.getLogger(__name__)

    logger.info("Loading trademark and user sheets...")
    my_sheet = pd.read_excel(trademark_sheet)
    user_sheet = pd.read_excel(user_sheet)

    results = pd.DataFrame()

    logger.info("Processing trademarks with phonetic and fuzzy matching...")

    try:
        for idx, my_row in my_sheet.iterrows():
            my_trademark = my_row['TM Applied For']
            my_app_number = my_row['TM Application No.']
            my_class = my_row['Class']

            if not isinstance(my_trademark, str):
                continue

            logger.info(f"\n[{idx + 1}/{len(my_sheet)}] Processing trademark: {my_trademark} (Class: {my_class})")

            matched_data = []
            for _, user_row in user_sheet.iterrows():
                user_trademark = user_row['TM Applied For']
                user_app_number = user_row['TM Application No.']
                user_class = user_row['Class']

                if not isinstance(user_trademark, str):
                    continue

                if user_class != my_class:
                    continue

                matched_phonetic = phonetic_substring_match(user_trademark, my_trademark)

                if matched_phonetic:
                    matched_data.append(user_app_number)  # Store the application number
                    logger.info(f"Phonetic match found with TM: {user_trademark}")

                else:
                    similarity_score = fuzz.ratio(my_trademark.lower(), user_trademark.lower())
                    if similarity_score >= similarity_threshold:
                        matched_data.append(user_app_number)  # Store the application number
                        logger.info(f"  Fuzzy match: '{user_trademark}' (Score: {similarity_score}, App No: {user_app_number})")

            if matched_data:
                row_data = {'TM Application No.': my_app_number}
                for i, app_num in enumerate(matched_data):
                    row_data[f'matched{i + 1}'] = app_num  # Store the matched application numbers

                results = pd.concat([results, pd.DataFrame([row_data])], ignore_index=True)

            if (idx + 1) % save_interval == 0:
                logger.info(f"Saving progress after {idx + 1} trademarks...")
                results.to_excel(output_sheet, index=False)

    except Exception as e:
        logger.error(f"\nAn error occurred: {str(e)}. Saving current progress...")
        results.to_excel(output_sheet, index=False)
        logger.info(f"Partial results saved to {output_sheet}. Exiting...")
        return

    logger.info(f"\nSaving final results to {output_sheet}...")
    results.to_excel(output_sheet, index=False)
    logger.info("Process complete!")
