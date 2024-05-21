# -*- coding: utf-8 -*-
import os

# Ustaw maksymalny rozmiar pliku w bajtach (np. 100 MB)
max_file_size = 100 * 1024 * 1024

# Funkcja do znalezienia dużych plików
def find_large_files(directory, max_size):
    large_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            file_path = os.path.join(root, file)
            if os.path.getsize(file_path) > max_size:
                large_files.append(file_path)
    return large_files

# Funkcja do dodania plików do .gitignore
def add_to_gitignore(files):
    with open('.gitignore', 'a') as gitignore:
        for file in files:
            gitignore.write(file + '\n')

# Główna część skryptu
if __name__ == "__main__":
    current_directory = os.getcwd()
    large_files = find_large_files(current_directory, max_file_size)
    add_to_gitignore(large_files)
    print(f"Added {len(large_files)} files to .gitignore")

