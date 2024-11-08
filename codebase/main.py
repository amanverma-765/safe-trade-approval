from doc_utils.increment_based_generator import application_number_gen
from doc_utils.pdf_utils import extract_pdf
from doc_utils.spreadsheet_utils import extract_excel
from fuzzer.similar_tm_extractor import extract_similar_tm
from trademark.webxela_automator import automate_webxela

print("------------------- Webxela Trademark Automator ------------------------")
print("")
print("Select options: ")
print("1. Extract Pdf")
print("2. Extract Excel")
print("3. Extract Trademark Data")
print("4. Generate Application Numbers Based on Increment")
print("5. Find Similar Trademarks")

option = input("Example: [1, 2, 3, 4, 5] > ")

if option == "1":

    pdf_location = input("Enter the location of PDF: ")
    if extract_pdf(pdf_location):
        print("PDF extracted")

elif option == "2":
    excel_location = input("Enter the location of Excel: ")
    if extract_excel(excel_location):
        print("Excel extracted")

elif option == "3":
    print("Automation will pick your previous pdf data as input")
    threads = int(input("Enter the numbers of threads to execute: "))
    automate_webxela(threads)
elif option == "4":
    amount = int(input("Enter how many new application numbers to generate: "))
    application_number_gen(amount)
elif option == "5":
    user_excel = input("Enter the location of your Excel: ")
    my_excel = 'data/extracted/combined_trademark_data.xlsx'
    extract_similar_tm(
        user_sheet=user_excel,
        trademark_sheet=my_excel
    )
else:
    print("Invalid option")

print("----------------------------- END ----------------------------------")