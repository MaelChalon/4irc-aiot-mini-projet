#include "MicroBit.h"
#include "bme280.h" 
#include "veml6070.h"
#include "tsl256x.h"
#include "ssd1306.h"
#include <string>

MicroBit uBit;
MicroBitI2C i2c(I2C_SDA0,I2C_SCL0);
MicroBitPin P8(MICROBIT_ID_IO_P8, MICROBIT_PIN_P8, PIN_CAPABILITY_DIGITAL_OUT);
ssd1306 screen(&uBit, &i2c, &P8);
int deviceId = 1;
ManagedString key = "cornichon"; // Clé de chiffrement Vigenère
ManagedString password = "password"; // Mot de passe pour l'authentificatio
ManagedString order = "THPUL"; // Variable pour stocker l'ordre reçu

//foncition pour crypter et decrypter
//true = decrypt
//false = encrypt
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

    uBit.serial.send(ManagedString("Message reçu : ") + message);

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
        order = decryptedMsg; // Mettre à jour l'ordre
        
    }
}

void sendData(ManagedString msg, int idCible) {
    ManagedString encryptedMsg = vigenereCipher(msg, false); // Chiffrement
    ManagedString encryptedPwd = vigenereCipher(password, false); // Chiffrement du mot de passe
    ManagedString radioMessage =    ManagedString("{") + ManagedString(idCible) + ManagedString(" : ") +  
                                    encryptedPwd + ManagedString(", ") +
                                    encryptedMsg + 
                                    ManagedString("}");
    uBit.serial.send(radioMessage); // Affichage du message chiffré sur le port série                              
    int retour = uBit.radio.datagram.send(radioMessage); // Envoi du message chiffré
    uBit.serial.send(retour); // Affichage du retour de l'envoi sur le port série
}

// Fonction ordre d'affichage sur l'écran
void displayOrder(ssd1306 &screen, ManagedString order, int tmp, int hum, int pres, int uv, uint32_t lux)
{
    int i = 0;
    for (i = 0; i < order.length(); i++)
    {
        char c = order.charAt(i);
        if (c == 'T')
        {
            screen.display_line(i, 0, (ManagedString("tmp : ") + ManagedString(tmp/100) + "." + (tmp > 0 ? ManagedString(tmp%100): ManagedString((-tmp)%100))).toCharArray());
        }
        else if (c == 'H')
        {
            screen.display_line(i, 0, (ManagedString("hum : ") + ManagedString(hum/100) + "." + ManagedString(tmp%100)).toCharArray());
        }
        else if (c == 'P')
        {
            screen.display_line(i, 0, (ManagedString("pres : ") + ManagedString(pres)).toCharArray());
        }
        else if (c == 'U')
        {
            screen.display_line(i, 0, (ManagedString("uv : ") + ManagedString(uv)).toCharArray());
        }
        else if (c == 'L')
        {
            screen.display_line(i, 0, (ManagedString("lum : ") + ManagedString((int)lux)).toCharArray());
        }
        else
        {
            screen.display_line(i, 0, (ManagedString("default : ") + ManagedString(c)).toCharArray());
        }
        screen.update_screen();
    }
    screen.update_screen();
}

ManagedString read_Data()
{
    bme280 bme(&uBit,&i2c);
    uint32_t pressure = 0;
    int32_t temp = 0;
    uint16_t humidite = 0;

    veml6070 veml(&uBit,&i2c);
    uint16_t uv = 0;

    tsl256x tsl(&uBit,&i2c);
    uint16_t comb =0;
    uint16_t ir = 0;
    uint32_t lux = 0;

    bme.sensor_read(&pressure, &temp, &humidite);
        int tmp = bme.compensate_temperature(temp);
        int pres = bme.compensate_pressure(pressure)/100;
        int hum = bme.compensate_humidity(humidite);

        veml.sensor_read(&uv);

        tsl.sensor_read(&comb, &ir, &lux);

        // Affichage des valeurs
        ManagedString msg = ManagedString(deviceId) + ManagedString(",") +
        order + ManagedString(",") +
        ManagedString(tmp/100) + "." + (tmp > 0 ? ManagedString(tmp%100): ManagedString((-tmp)%100)) + ManagedString(",") +
        ManagedString(hum/100) + "." + ManagedString(tmp%100) + ManagedString(",") +
        ManagedString(pres)+ ManagedString(",") +
        ManagedString(uv) + ManagedString(",") +
        ManagedString((int)lux);

        displayOrder(screen, order, tmp, hum, pres, uv, lux);
        uBit.serial.send(msg); // Affichage du message sur le port série

        return msg;
}




int main() {
    // Initialise the micro:bit runtime.
    uBit.init();
    uBit.serial.baud(115200); // Configure la vitesse de la sortie série
    
    uBit.radio.enable();
    uBit.radio.setGroup(27); // Définit le groupe radio
    // Initialise le module radio 
    uBit.messageBus.listen(MICROBIT_ID_RADIO, MICROBIT_RADIO_EVT_DATAGRAM, onDataReceved);

    while(true)
    {
        ManagedString msg = read_Data();
        sendData(msg, 0); // Envoi du message chiffré
    }

    release_fiber();
}