
def read_last_application_id(progress_file):
    try:
        with open(progress_file, 'r') as file:
            last_id = int(file.read().strip())
    except FileNotFoundError:
        last_id = 1000000
    return last_id


def write_last_application_id(progress_file, last_id):
    with open(progress_file, 'w') as file:
        file.write(str(last_id))


def application_number_gen(amount: int):

    progress_file = "data/application_num/progress"
    output_file = "data/application_num/application_numbers.txt"

    last_id = read_last_application_id(progress_file)

    with open(output_file, 'w') as file:
        for i in range(amount):
            new_id = last_id + (i + 1)
            file.write(f"{new_id}\n")

    write_last_application_id(progress_file, new_id)

    print(f"{amount} application numbers generated and written to {output_file}")
    print(f"Last generated Application ID updated to {new_id} in {progress_file}")

