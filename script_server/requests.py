import re
import json

def parse_line_to_dict(line):
    match = re.search(r'\{.*?\}', line)
    if match:
        # Ajouter des guillemets autour des clés
        json_compatible = re.sub(r'(\w+):', r'"\1":', match.group())
        try:
            return json.loads(json_compatible)
        except json.JSONDecodeError as e:
            print(e)
            pass
    return None

def get_devices(filepath):
    ids = set()
    with open(filepath, 'r', encoding='utf-8') as file:
        for line in file:
            data = parse_line_to_dict(line)
            if data and 'id' in data:
                ids.add(data['id'])
    return ids

# Fonction pour obtenir la dernière valeur d'un ID donné
def get_values(device_id, filepath):
    last_match = None
    with open(filepath, 'r', encoding='utf-8') as file:
        for line in file:
                    data = parse_line_to_dict(line)
                    if data and 'id' in data:
                        if data['id'] == device_id:
                            last_match = data
    return last_match


print(get_values(53,"values.txt"))