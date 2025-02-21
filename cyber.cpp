#include <iostream>
#include <string>
#include <vector>
#include <cctype>

using namespace std;

// Function to convert a letter key to shift values (0-25 based on 'a' = 0, ..., 'z' = 25)
vector<int> convertKeyToShifts(const string &key) {
    vector<int> shifts;
    for (char c : key) {
        shifts.push_back(tolower(c) - 'a'); // Convert to lowercase for uniformity
    }
    return shifts;
}

// Function to apply the substitution cipher (case-sensitive)
string applySubstitution(const string &plaintext) {
    string cipherText = "";
    for (char c : plaintext) {
        if (islower(c)) {
            switch (c) {
                case 'a': cipherText += "!"; break;
                case 'b': cipherText += "\""; break;
                case 'c': cipherText += "#"; break;
                case 'd': cipherText += "$"; break;
                case 'e': cipherText += "%"; break;
                case 'f': cipherText += "&"; break;
                case 'g': cipherText += "'"; break;
                case 'h': cipherText += "("; break;
                case 'i': cipherText += ")"; break;
                case 'j': cipherText += "*"; break;
                case 'k': cipherText += "+"; break;
                case 'l': cipherText += ","; break;
                case 'm': cipherText += "-"; break;
                case 'n': cipherText += "."; break;
                case 'o': cipherText += "/"; break;
                case 'p': cipherText += "0"; break;
                case 'q': cipherText += "1"; break;
                case 'r': cipherText += "2"; break;
                case 's': cipherText += "3"; break;
                case 't': cipherText += "4"; break;
                case 'u': cipherText += "5"; break;
                case 'v': cipherText += "6"; break;
                case 'w': cipherText += "7"; break;
                case 'x': cipherText += "8"; break;
                case 'y': cipherText += "9"; break;
                case 'z': cipherText += ":"; break;
                default: cipherText += c; break;
            }
        } else if (isupper(c)) {
            switch (c) {
                case 'A': cipherText += "A!"; break;
                case 'B': cipherText += "B\""; break;
                case 'C': cipherText += "C#"; break;
                case 'D': cipherText += "D$"; break;
                case 'E': cipherText += "E%"; break;
                case 'F': cipherText += "F&"; break;
                case 'G': cipherText += "G'"; break;
                case 'H': cipherText += "H("; break;
                case 'I': cipherText += "I)"; break;
                case 'J': cipherText += "J*"; break;
                case 'K': cipherText += "K+"; break;
                case 'L': cipherText += "L,"; break;
                case 'M': cipherText += "M-"; break;
                case 'N': cipherText += "N."; break;
                case 'O': cipherText += "O/"; break;
                case 'P': cipherText += "P0"; break;
                case 'Q': cipherText += "Q1"; break;
                case 'R': cipherText += "R2"; break;
                case 'S': cipherText += "S3"; break;
                case 'T': cipherText += "T4"; break;
                case 'U': cipherText += "U5"; break;
                case 'V': cipherText += "V6"; break;
                case 'W': cipherText += "W7"; break;
                case 'X': cipherText += "X8"; break;
                case 'Y': cipherText += "Y9"; break;
                case 'Z': cipherText += "Z:"; break;
                default: cipherText += c; break;
            }
        } else {
            cipherText += c;
        }
    }
    return cipherText;
}

// Function to apply the shift cipher using the key as letter-based shifts
string applyShift(const string &text, const string &key) {
    string shiftedText = "";
    vector<int> shifts = convertKeyToShifts(key);
    int keyLength = shifts.size();
    int keyIndex = 0;

    for (char c : text) {
        int shift = shifts[keyIndex];
        shiftedText += (char)(c + shift);
        keyIndex = (keyIndex + 1) % keyLength;
    }

    return shiftedText;
}

// Function to reverse the shift
string reverseShift(const string &shiftedText, const string &key) {
    string originalText = "";
    vector<int> shifts = convertKeyToShifts(key);
    int keyLength = shifts.size();
    int keyIndex = 0;

    for (char c : shiftedText) {
        int shift = shifts[keyIndex];
        originalText += (char)(c - shift);
        keyIndex = (keyIndex + 1) % keyLength;
    }

    return originalText;
}

// Function to apply transposition and display column-by-column output
string applyTransposition(const string &shiftedText) {
    int rows = 3;
    int cols = (shiftedText.length() + rows - 1) / rows;

    vector<vector<char>> grid(rows, vector<char>(cols, ' '));
    int index = 0;

    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols && index < shiftedText.size(); ++j) {
            grid[i][j] = shiftedText[index++];
        }
    }

    cout << "\nTransposition Step:\nArranged in a " << rows << "x" << cols << " grid:\n";
    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
            cout << grid[i][j] << " ";
        }
        cout << endl;
    }

    cout << "\nReading column by column:\n";
    for (int j = 0; j < cols; ++j) {
        cout << "Column " << j + 1 << ": ";
        for (int i = 0; i < rows; ++i) {
            if (grid[i][j] != ' ') {
                cout << grid[i][j] << " ";
            }
        }
        cout << endl;
    }

    string cipherText = "";
    for (int j = 0; j < cols; ++j) {
        for (int i = 0; i < rows; ++i) {
            cipherText += grid[i][j];
        }
    }

    return cipherText;
}

// Function to reverse the transposition
string reverseTransposition(const string &cipherText) {
    int rows = 3;
    int cols = (cipherText.length() + rows - 1) / rows;

    vector<vector<char>> grid(rows, vector<char>(cols, ' '));
    int index = 0;

    for (int j = 0; j < cols; ++j) {
        for (int i = 0; i < rows && index < cipherText.size(); ++i) {
            grid[i][j] = cipherText[index++];
        }
    }

    string plainText = "";
    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
            plainText += grid[i][j];
        }
    }

    return plainText;
}

// Main function with encryption and decryption flow
int main() {
    string plaintext, key;
    cout << "Enter plaintext: ";
    getline(cin, plaintext);

    cout << "Enter key (a sequence of letters, e.g., key): ";
    getline(cin, key);

    cout << "\nOriginal Text: " << plaintext << endl;

    // Step 1: Apply substitution cipher
    string substitutedText = applySubstitution(plaintext);
    cout << "Substituted Text: " << substitutedText << endl;

    // Step 2: Apply shift using letter-based key
    string shiftedText = applyShift(substitutedText, key);
    cout << "Shifted Text with key (" << key << "): " << shiftedText << endl;

    // Step 3: Apply transposition cipher
    string transposedText = applyTransposition(shiftedText);
    cout << "Transposed Ciphertext: " << transposedText << endl <<"\n";

    // Decryption Steps
    string reversedTransposition = reverseTransposition(transposedText);
    cout << "After Reversing Transposition: " << reversedTransposition << endl;

    string reversedShift = reverseShift(reversedTransposition, key);
    cout << "After Reversing Shift: " << reversedShift << endl;

    cout << "Original Text (after decryption): " << reversedShift << endl;

    cout << "PlainText : " << plaintext;

    return 0;
}
