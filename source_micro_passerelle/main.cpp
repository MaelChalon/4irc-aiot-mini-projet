/*
The MIT License (MIT)

Copyright (c) 2016 British Broadcasting Corporation.
This software is provided by Lancaster University by arrangement with the BBC.

Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.
*/

#include "MicroBit.h"
#include <string>


MicroBit uBit;
int deviceId = 0;
int temperature = 0;
int humidity = 1;
int pressure = 2;

ManagedString key = "cornichon"; // Clé de chiffrement Vigenère
ManagedString password = "password"; // Mot de passe

#include "MicroBit.h"

// Vigenère : chiffrement si decrypt = false, déchiffrement si true
ManagedString vigenereCipher(ManagedString input, bool decrypt) {
    int textLen = input.length();
    int keyLen = key.length();
    char *output = new char[textLen + 1];

    for (int i = 0; i < textLen; i++) {
        char t = input.charAt(i);
        char k = key.charAt(i % keyLen);
        char result;

        if (t == '\r' || t == '\n') {
            output[i] = t; // Conserver les caractères de retour à la ligne
            continue; // Passer à l'itération suivante
        }

        if (decrypt) {
            result = (t - k + 256) % 256; // décalage inverse
        } else {
            result = (t + k) % 256; // décalage direct
        }

        output[i] = result;
    }

    output[textLen] = '\0';

    ManagedString resultStr(output);
    delete[] output;
    return resultStr;
}

int indexOfChar(ManagedString str, char c) {
    for (int i = 0; i < str.length(); i++) {
        if (str.charAt(i) == c) {
            return i;
        }
    }
    return -1;
}


void onDataReceved(MicroBitEvent)
{
    // id,ordre,température,humidité,pression,uv,lux
    ManagedString message = uBit.radio.datagram.recv();

    // Extraction de l'identifiant cible
    int openBrace = indexOfChar(message, '{');
    int colon = indexOfChar(message, ':');
    int comma = indexOfChar(message, ',');
    int closeBrace = indexOfChar(message, '}');

    if (openBrace == -1 || colon == -1 || closeBrace == -1 || comma == -1 || colon <= openBrace || closeBrace <= colon || colon <= openBrace || closeBrace <= colon || comma <= colon)
        return; // Format invalide, on ne fait rien

    ManagedString idStr = message.substring(openBrace + 1, colon - (openBrace + 1));
    ManagedString passwordStr = message.substring(colon + 2, comma - (colon + 2));
    ManagedString payload = message.substring(comma + 2, closeBrace - (comma + 2));

    int idCible = atoi((char *)idStr.toCharArray());
    ManagedString decryptedPwd = vigenereCipher(passwordStr, true); // Déchiffrement du mot de passe
    // Vérification de l'ID
    if ((idCible == deviceId || idCible == -1) and decryptedPwd == password)
    {
        ManagedString decryptedMsg = vigenereCipher(payload, true);
        uBit.serial.send(decryptedMsg + '\n');
    }
}

void sendData(ManagedString msg, int idCible) {
    ManagedString encryptedMsg = vigenereCipher(msg, false); // Chiffrement
    ManagedString encryptedPwd = vigenereCipher(password, false); // Chiffrement du mot de passe
    ManagedString radioMessage =    ManagedString("{") + ManagedString(idCible) + ManagedString(" : ") +  
                                    encryptedPwd + ManagedString(", ") +
                                    encryptedMsg + 
                                    ManagedString("}");
    int retour = uBit.radio.datagram.send(radioMessage); // Envoi du message chiffré
    uBit.serial.send(retour);
}

int main() {
    // Initialise le runtime
    uBit.init();
    uBit.serial.baud(115200); // Configure la vitesse de la sortie série

    uBit.radio.enable();
    uBit.radio.setGroup(27); // Définit le groupe radio

    uBit.messageBus.listen(MICROBIT_ID_RADIO, MICROBIT_RADIO_EVT_DATAGRAM, onDataReceved);


    // Boucle infinie pour envoyer des valeurs sur la sortie série
    while(1){
        uBit.sleep(1000);

        // ManagedString msg = ManagedString("{id: ") + ManagedString(id) + 
        // ManagedString(", temperature: ") + ManagedString(temperature) + 
        // ManagedString(", humidity: ") + ManagedString(humidity) + 
        // ManagedString(", pressure: ") + ManagedString(pressure) + 
        // ManagedString("}\r\n");

        // uBit.serial.send(msg);

        ManagedString input = uBit.serial.readUntil('\n');
        // on split l'ordre et la cible du message de la liaison série
        int comma = indexOfChar(input, ',');
        ManagedString cible = input.substring(1, comma);
        ManagedString ordre = input.substring(comma + 1, (input.length() - comma));
        sendData(ordre, atoi(cible.toCharArray()));
    }

    release_fiber();
}