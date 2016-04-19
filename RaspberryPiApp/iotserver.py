#!/usr/bin/python
import Adafruit_DHT
from flask import Flask, render_template, jsonify, abort, Response
import spidev
import time
import os
import threading
import requests
import picamera
import io


class Camera(threading.Thread):
	
	frame = None
	last_access = None

	def __init__(self):
		threading.Thread.__init__(self)
		self._stop = threading.Event()

	def run(self):
		with picamera.PiCamera() as camera:
			camera.resolution = (344, 344)
			camera.start_preview()
			time.sleep(1) #Camera warmup time
			stream = io.BytesIO()
			for foo in camera.capture_continuous(stream, 'jpeg', use_video_port=True, quality=20):
				stream.seek(0)
				self.frame = stream.read()
				stream.seek(0)
				stream.truncate()
				if self.last_access is not None:
					if time.time() - self.last_access > 2:
						camera.close()
						break
				if self._stop.isSet():
					camera.close()
					break
			 

	def getFrame(self):
		while self.frame is None:
			time.sleep(0)
		self.last_access = time.time()
		return self.frame

	def stop(self):
		print("stopping")
		self._stop.set()
		

	

# Function to read SPI data from MCP3008 chip
# Channel must be an integer 0-7
def readChannel(channel):
	try:
		channel = int(channel)
		if channel < 0 or channel > 7:
			raise Exception('invalid channel')
		adc = spi.xfer2([1,(8+channel)<<4,0])
		data = ((adc[1]&3) << 8) + adc[2]
		# Sensor data is sampled in 10bits i.e 1024 steps, and will be a value between 0-1023 
 	 	# wet low, dry high - bright low, dark high
 	 	# Lets flip it so it goes from dry to wet, dark to bright
		return 1023 - data
	except:
		print "Channel was not a integer between 0-7"
		return None



# Video generator function
def vidGen(cam):
	cam.start()
	while True:
		frame = cam.getFrame()
		try:
			yield (b'--frame\r\n' b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')
		except:
			cam.stop()
			break





###############
## MAIN PROGRAM
################

#Using an MCP3008 AC-converter with SPI-interface (soil moisture)
spi = spidev.SpiDev()
channel_moisture = 0
channel_light = 1
# Using DHT11-interface and port GPIO17 (temperature and humidity)
sensor = Adafruit_DHT.DHT11
pin = 17
print "# IoT!:  Sensor: DHT{} and pin is {}".format(sensor, pin)

# Creating the server app
app = Flask(__name__)




## Routes for the server app
#
# Main response with sensor data i json 
@app.route("/")
def sensordata():
	# First grab the SPI sensor data from channel 0
	spi.open(0,0)
  	moisture = readChannel(channel_moisture)
	# Grab light sensor data through SPI from channel 1
	light = readChannel(channel_light)
	spi.close()
	
	# Try to grab a DHT11-sensor reading.  Use the read_retry method which will retry up
	# to 15 times to get a sensor reading (waiting 2 seconds between each retry).
	humidity, temperature = Adafruit_DHT.read_retry(sensor, pin)
	print "reading the pin is done..."
	# Note that sometimes you won't get a reading and
	# the results will be null (because Linux can't
	# guarantee the timing of calls to read the sensor).  
	# If this happens raise 500 internal server error
	if moisture is not None  and light is not None and humidity is not None and temperature is not None:
		light = int(float(light)/1023*100)
		response = {
			'soil moisture' : moisture,
			'light' : light,
			'temperature' : temperature, 
			'humidity' : humidity
			}
		return jsonify(response)
	else:
		abort(500)

# Wrapping the video stream in a html tag
@app.route("/videoplayer")
def videoplayer():
	return render_template("videoplayer.html")

# Mjpeg stream response
@app.route("/video")
def video_feed():
	return Response(vidGen(Camera()),
			mimetype='multipart/x-mixed-replace; boundary=frame')


if __name__ == "__main__":	
	# Run up the server
	app.run(host="0.0.0.0", port=5000, debug=True, use_reloader=False)
