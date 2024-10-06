import csv
import logging
import pandas as pd
import os


def save_to_csv(data: dict, filename: str = 'data/extracted/trademark_data.csv'):
    file_exists = os.path.exists(filename)

    with open(filename, mode='a', newline='', encoding='utf-8') as csvfile:
        field_names = data.keys()
        writer = csv.DictWriter(csvfile, fieldnames=field_names)

        if not file_exists:
            writer.writeheader()

        writer.writerow(data)



def save_to_excel(data: dict, filename: str):
    file_exists = os.path.exists(filename)

    df = pd.DataFrame([data])

    if file_exists:
        with pd.ExcelWriter(filename, mode='a', engine='openpyxl', if_sheet_exists='overlay') as writer:
            df.to_excel(writer, index=False, header=False, startrow=writer.sheets['Sheet1'].max_row)
    else:
        df.to_excel(filename, index=False)




def extract_excel(excel_file: str) -> bool:
    try:
        df = pd.read_excel(excel_file, engine='openpyxl')

        first_column = df.iloc[:, 0]

        with open('data/application_num/application_numbers.txt', 'w', encoding='utf-8') as f:
            for value in first_column:
                f.write(f"{value}\n")

        return True

    except Exception as e:
        logging.error(f"Error while extracting data from excel file: {e}")
        return False




def combine_excel_files(
        output_file: str = 'data/extracted/combined_trademark_data.xlsx'
):
    directory = 'data/extracted/'

    combined_df = pd.DataFrame()

    if os.path.exists(output_file):
        existing_df = pd.read_excel(output_file, engine='openpyxl')
        combined_df = pd.concat([combined_df, existing_df], ignore_index=True)

    for filename in os.listdir(directory):
        if filename.startswith('trademark_data_thread_') and filename.endswith('.xlsx'):
            file_path = os.path.join(directory, filename)

            df = pd.read_excel(file_path, engine='openpyxl')

            combined_df = pd.concat([combined_df, df], ignore_index=True)

            os.remove(file_path)

    combined_df.to_excel(output_file, index=False)

    logging.info(f"Combined data saved to {output_file}")