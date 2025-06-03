# Program to control passerelle between Android application
# and micro-controller through USB tty

import socketserver
import serial
import threading
from fonction_requests import get_devices, get_values, vigenere_decrypt

HOST           = "0.0.0.0"
UDP_PORT       = 10000
FILENAME        = "values.txt"
LAST_VALUE      = ""

class ThreadedUDPRequestHandler(socketserver.BaseRequestHandler):

    def handle(self):
        data = self.request[0].decode('utf-8').strip()
        socket = self.request[1]
        current_thread = threading.current_thread()
        data = vigenere_decrypt(data, "HERMES")
        print("{}: client: {}, wrote: {}".format(current_thread.name, self.client_address, data))
        if data != "":                             
                        if data == "getValues()": # Sent last value received from micro-controller
                                print("envoie du message à " + self.client_address)
                                socket.sendto(LAST_VALUE, self.client_address)
                                # TODO: Create last_values_received as global variable      

                        elif data.split()[0] == "GETVALUES":
                                print(f"Envoie des valeurs de {data.split()[1]}")
                                print(bytes(get_values(data.split()[1], FILENAME), "utf-8"))
                                socket.sendto(bytes(get_values(data.split()[1], FILENAME), "utf-8"), self.client_address)
                        elif data == "GETDEVICES":
                                print("envoie de la liste des devices")
                                socket.sendto(bytes(get_devices(FILENAME), "utf-8"), self.client_address)
                        elif data.split()[0] == "PUTORDER":
                                if set("TPHUL").issubset(set(data.split()[2])):
                                        sendUARTMessage(f"{data.split()[1]},{data.split()[2]}\n")
                        elif data == "ISAVAILABLE":
                                socket.sendto(bytes("ok", "utf-8"), self.client_address)
                        else:
                                print("Unknown message: ", data)

class ThreadedUDPServer(socketserver.ThreadingMixIn, socketserver.UDPServer):
    pass


# send serial message 
SERIALPORT = "COM3"
BAUDRATE = 115200
ser = serial.Serial()

def initUART():        
        # ser = serial.Serial(SERIALPORT, BAUDRATE)
        ser.port=SERIALPORT
        ser.baudrate=BAUDRATE
        ser.bytesize = serial.EIGHTBITS #number of bits per bytes
        ser.parity = serial.PARITY_NONE #set parity check: no parity
        ser.stopbits = serial.STOPBITS_ONE #number of stop bits
        ser.timeout = None          #block read

        # ser.timeout = 0             #non-block read
        # ser.timeout = 2              #timeout block read
        ser.xonxoff = False     #disable software flow control
        ser.rtscts = False     #disable hardware (RTS/CTS) flow control
        ser.dsrdtr = False       #disable hardware (DSR/DTR) flow control
        #ser.writeTimeout = 0     #timeout for write
        print('Starting Up Serial Monitor')
        try:
                ser.open()
        except serial.SerialException:
                print("Serial {} port not available".format(SERIALPORT))
                exit()



def sendUARTMessage(msg):
    ser.write(msg.encode())
    print("Message <" + msg + "> sent to micro-controller." )


# Main program logic follows:
if __name__ == '__main__':
        initUART()
        f= open(FILENAME,"a")
        print ('Press Ctrl-C to quit.')
        tab = []
        server = ThreadedUDPServer((HOST, UDP_PORT), ThreadedUDPRequestHandler)

        server_thread = threading.Thread(target=server.serve_forever)
        server_thread.daemon = True

        try:
                server_thread.start()
                print("Server started at {} port {}".format(HOST, UDP_PORT))
                
                while ser.isOpen() : 
                        # time.sleep(100)
                        if (ser.inWaiting() > 0): # if incoming bytes are waiting
                                f= open(FILENAME,"a")
                                data_bytes = ser.read(ser.inWaiting())
                                print(data_bytes)
                                data_str = data_bytes.decode()
                                f.write(data_str)
                                f.close()
                                # LAST_VALUE = data_str
                
                                
        except (KeyboardInterrupt, SystemExit):
                server.shutdown()
                server.server_close()
                f.close()
                ser.close()
                exit()