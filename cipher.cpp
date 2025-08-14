#include <iostream>
#include <string>
#include <vector>
#include <cctype>
#include <map>
#include <algorithm>
#include <fstream>   // For file input/output
#include <sstream>   // For reading file content easily

using namespace std;

// --- Data and Setup ---
map<char, string> substitutionMap = {
    {'a', "!"}, {'b', "\""}, {'c', "#"}, {'d', "$"}, {'e', "%"}, {'f', "&"},
    {'g', "'"}, {'h', "("}, {'i', ")"}, {'j', "*"}, {'k', "+"}, {'l', ","},
    {'m', "-"}, {'n', "."}, {'o', "/"}, {'p', "0"}, {'q', "1"}, {'r', "2"},
    {'s', "3"}, {'t', "4"}, {'u', "5"}, {'v', "6"}, {'w', "7"}, {'x', "8"},
    {'y', "9"}, {'z', ":"}
};

map<string, char> reverseSubstitutionMap;

void createReverseMap() {
    for (auto const& [key, val] : substitutionMap) {
        reverseSubstitutionMap[val] = key;
    }
}

// --- Helper functions for file operations ---

// Reads all text from a given filename.
string readFileContent(const string& filename) {
    ifstream file(filename);
    if (!file.is_open()) {
        cerr << "Error: Could not open file '" << filename << "'. Please make sure it exists in the same folder." << endl;
        return "";
    }
    stringstream buffer;
    buffer << file.rdbuf();
    return buffer.str();
}

// NEW: This function silently writes text to a file.
void writeFileContent(const string& filename, const string& content) {
    ofstream file(filename);
    if (!file.is_open()) {
        // We keep this silent as requested, but in a real app, logging this would be good.
        return;
    }
    file << content;
}


// --- Encryption Functions (Unchanged) ---

string applySubstitution(const string& plaintext) {
    string cipherText = "";
    for (char c : plaintext) {
        if (islower(c)) {
            if (substitutionMap.count(c)) {
                cipherText += substitutionMap[c];
            } else {
                cipherText += c;
            }
        } else if (isupper(c)) {
            char lower_c = tolower(c);
            if (substitutionMap.count(lower_c)) {
                cipherText += c + substitutionMap[lower_c];
            } else {
                cipherText += c;
            }
        } else {
            cipherText += c;
        }
    }
    return cipherText;
}

string applyShift(const string& text, const string& key) {
    string shiftedText = "";
    vector<int> shifts;
    for (char k : key) { shifts.push_back(tolower(k) - 'a'); }

    int keyIndex = 0;
    for (char c : text) {
        int shift = shifts[keyIndex % shifts.size()];
        shiftedText += (char)(c + shift);
        keyIndex++;
    }
    return shiftedText;
}

string applyTransposition(const string& shiftedText) {
    const int rows = 3;
    int cols = (shiftedText.length() + rows - 1) / rows;
    vector<vector<char>> grid(rows, vector<char>(cols, ' '));
    int index = 0;

    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols && index < shiftedText.size(); ++j) {
            grid[i][j] = shiftedText[index++];
        }
    }

    cout << "\nTransposition Step (Encryption):\nArranged in a " << rows << "x" << cols << " grid:\n";
    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
            cout << grid[i][j] << " ";
        }
        cout << endl;
    }

    cout << "\nReading column by column to create ciphertext...\n";
    string cipherText = "";
    for (int j = 0; j < cols; ++j) {
        for (int i = 0; i < rows; ++i) {
            cipherText += grid[i][j];
        }
    }
    return cipherText;
}


// --- Decryption Functions (Unchanged) ---

string reverseTransposition(const string& cipherText, int originalLength) {
    const int rows = 3;
    int cols = (originalLength + rows - 1) / rows;
    vector<vector<char>> grid(rows, vector<char>(cols, ' '));
    int index = 0;

    cout << "\nReversing Transposition (Decryption):\nRe-filling the " << rows << "x" << cols << " grid column by column:\n";
    for (int j = 0; j < cols; ++j) {
        for (int i = 0; i < rows && index < cipherText.size(); ++i) {
            grid[i][j] = cipherText[index++];
        }
    }
    
    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
            cout << grid[i][j] << " ";
        }
        cout << endl;
    }

    cout << "\nReading row by row to restore text...\n";
    string plainText = "";
    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
            plainText += grid[i][j];
        }
    }
    
    cout << "Trimming text back to original length of " << originalLength << ".\n";
    return plainText.substr(0, originalLength);
}

string reverseShift(const string& shiftedText, const string& key) {
    string originalText = "";
    vector<int> shifts;
    for (char k : key) { shifts.push_back(tolower(k) - 'a'); }
    
    int keyIndex = 0;
    for (char c : shiftedText) {
        int shift = shifts[keyIndex % shifts.size()];
        originalText += (char)(c - shift);
        keyIndex++;
    }
    return originalText;
}

string reverseSubstitution(const string& cipherText) {
    string plainText = "";
    cout << "\nReversing Substitution:\n";
    for (int i = 0; i < cipherText.length(); ++i) {
        char c = cipherText[i];
        if (isupper(c) && i + 1 < cipherText.length()) {
            string next_char_str = string(1, cipherText[i+1]);
            if (reverseSubstitutionMap.count(next_char_str)) {
                 cout << "  - Found pair '" << c << next_char_str << "'. Reverting to '" << c << "'.\n";
                 plainText += c;
                 i++;
                 continue;
            }
        }
        
        string sub_key = string(1, c);
        if (reverseSubstitutionMap.count(sub_key)) {
            cout << "  - Found symbol '" << sub_key << "'. Reverting to '" << reverseSubstitutionMap[sub_key] << "'.\n";
            plainText += reverseSubstitutionMap[sub_key];
        } else {
            plainText += c;
        }
    }
    return plainText;
}


int main() {
    createReverseMap();

    string inputFilename;
    cout << "Enter the name of the input file (e.g., input.txt): ";
    cin >> inputFilename;
    cin.ignore(); 

    string plaintext = readFileContent(inputFilename);
    if (plaintext.empty() && !inputFilename.empty()) {
        return 1; 
    }

    string key;
    cout << "Enter key (a sequence of letters): ";
    getline(cin, key);

    cout << "\n--- ENCRYPTION ---" << endl;
    cout << "Original Text from file '" << inputFilename << "': " << plaintext << endl;

    string substitutedText = applySubstitution(plaintext);
    cout << "After Substitution: " << substitutedText << endl;

    string shiftedText = applyShift(substitutedText, key);
    cout << "After Shift with key (" << key << "): " << shiftedText << endl;
    
    string transposedText = applyTransposition(shiftedText);
    cout << "Final Transposed Ciphertext: " << transposedText << endl;

    // NEW: Silently write the encrypted text to a file.
    writeFileContent("encrypt.txt", transposedText);

    cout << "\n\n--- DECRYPTION ---" << endl;
    
    string reversedTransposition = reverseTransposition(transposedText, shiftedText.length());
    cout << "After Reversing Transposition: " << reversedTransposition << endl;

    string reversedShift = reverseShift(reversedTransposition, key);
    cout << "After Reversing Shift: " << reversedShift << endl;

    string finalPlainText = reverseSubstitution(reversedShift);
    cout << "After Reversing Substitution: " << finalPlainText << endl;

    // NEW: Silently write the final decrypted text to a file.
    writeFileContent("original.txt", finalPlainText);

    cout << "\n=================================" << endl;
    cout << " Original Plaintext:  " << plaintext << endl;
    cout << " Final Decrypted Text: " << finalPlainText << endl;
    cout << "==================================" << endl;

    return 0;
}
