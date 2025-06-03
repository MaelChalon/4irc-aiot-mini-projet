import base64
import hashlib
import json
from cryptography.fernet import Fernet


def parse_sensor_string(line):
    parts = line.strip().split(",")
    if len(parts) != 7:
        # print("Format invalide, 7 éléments requis")
        return None

    return {
        "id": int(parts[0]),
        "order": parts[1],
        "temperature": float(parts[2]),
        "humidity": float(parts[3]),
        "pressure": int(parts[4]),
        "uv": int(parts[5]),
        "light": int(parts[6])
    }


def vigenere_encrypt(plain_text, key):
    encrypted = []
    key = key.upper()
    for i, c in enumerate(plain_text.upper()):
        if c.isalpha():
            shift = ord(key[i % len(key)]) - ord('A')
            enc = (ord(c) - ord('A') + shift) % 26
            encrypted.append(chr(enc + ord('A')))
        else:
            encrypted.append(c)
    return ''.join(encrypted)


def vigenere_decrypt(cipher_text, key):
    decrypted = []
    key = key.upper()
    for i, c in enumerate(cipher_text):
        if c.isalpha():
            shift = ord(key[i % len(key)]) - ord('A')
            dec = (ord(c) - ord('A') - shift) % 26
            decrypted.append(chr(dec + ord('A')))
        else:
            decrypted.append(c)
    return ''.join(decrypted)


def get_devices(filepath, passphrase="Apollon"):
    ids = set()
    with open(filepath, 'r', encoding='utf-8') as file:
        for line in file:
            line_traitee = parse_sensor_string(line)
            if line_traitee is not None:
                if line_traitee and 'id' in line_traitee:
                    ids.add(line_traitee['id'])
    x = {
         "list": list(ids)
    }
    json_str = json.dumps(x)
    return vigenere_encrypt(json_str, passphrase)


# Fonction pour obtenir la dernière valeur d'un ID donné
def get_values(device_id, filepath, passphrase="Apollon"):
    last_match = None
    with open(filepath, 'r', encoding='utf-8') as file:
        for line in file:
            line_traitee = parse_sensor_string(line)
            if line_traitee and "id" in line_traitee:
                if str(line_traitee["id"]) == str(device_id):
                    last_match = line_traitee

    if last_match is not None:
        json_str = json.dumps(last_match)
        return vigenere_encrypt(json_str, passphrase)
    else:
        return None


# print(get_devices("values.txt"))
# print(vigenere_decrypt(get_devices("values.txt"), "Apollon" ))
# print(get_values(2,"values.txt"))
# print(vigenere_decrypt(get_values(2,"values.txt"), "Apollon"))
