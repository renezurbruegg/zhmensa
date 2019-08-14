# !/usr/bin/env python
# -*- coding: utf-8 -*-
"""Entry point for the server application."""
import threading
import json
import logging
from pylogging import HandlerType, setup_logger
import time
import os
import requests
import sys
from datetime import date
from html.parser import HTMLParser
import feedparser


from flask_cors import CORS
from pymongo import MongoClient
from .config import CONFIG
import traceback
from flask import request, jsonify, current_app, Flask
from flask_jwt_simple import (
    JWTManager, jwt_required, create_jwt, get_jwt_identity
)

from .http_codes import Status
from flask import make_response

from datetime import timedelta, datetime
from functools import update_wrapper


logger = logging.getLogger(__name__)
setup_logger(log_directory='./logs', file_handler_type=HandlerType.ROTATING_FILE_HANDLER, allow_console_logging = True, console_log_level  = logging.DEBUG, max_file_size_bytes = 1000000)

app = Flask(__name__)
# Load Configuration for app. Secret key etc.
config_name = os.getenv('FLASK_CONFIGURATION', 'default')

app.config.from_object(CONFIG[config_name])

# Set Cors header. Used to accept connections from browser using XHTTP requests.
CORS(app, headers=['Content-Type'])
jwt = JWTManager(app)

mensaMapping = {}

mydb = MongoClient("localhost", 27017)["zhmensa"]


def main():
    """Main entry point of the app. """

    logger.info("starting server")
    try:
        app.run(debug = True, host = app.config["IP"], port = app.config["PORT"])
        logger.info("Server started. IP: " + str(app.config["IP"]) + " Port: " + str(app.config["PORT"]))
    except Exception as exc:
        logger.error(exc)
        logger.exception(traceback.format_exc())
    finally:
        pass

    #

def loadDayIntoMensaMap(date, db, mensaMap):
    collection = db["menus"]
    mensa = None

    for menu in collection.find({"date":str(date)}).sort("mensaName"):
        if(mensa is None or mensa.name != menu["mensaName"]):
            mensa = mensaMap[menu["mensaName"]]
        mensa.addMenuFromDb(menu, date)

def loadMensaMapFromDb(db):
    today = date.today()

    if(today.weekday() < 5):
        startOfWeek = today - timedelta(days = today.weekday())
    else: # It is saturday or sunday, load menus for next weekl
        startOfWeek = today + timedelta(days = -today.weekday())

    mensaMap = getEmptyMensaMapFromDb(db)

    for day in range(5):
        loadDayIntoMensaMap(startOfWeek + timedelta(days=day), db, mensaMap)

    json_data = json.dumps(mensaMap, cls=CustomJsonEncoder,indent=2, sort_keys=False)
    print(json_data)


@jwt.jwt_data_loader
def add_claims_to_access_token(identity):
    """ Used to allow CORS Request from any source"""
    now = datetime.utcnow()
    return {
        'exp': now + current_app.config['JWT_EXPIRES'],
        'iat': now,
        'nbf': now,
        'sub': identity,
        'roles': 'Admin'
    }


def crossdomain(origin=None, methods=None, headers=None, max_age=21600,
                attach_to_all=True, automatic_options=True):
    """Decorator function that allows crossdomain requests.
      Courtesy of
      https://blog.skyred.fi/articles/better-crossdomain-snippet-for-flask.html
    """
    if methods is not None:
        methods = ', '.join(sorted(x.upper() for x in methods))
    # use str instead of basestring if using Python 3.x
    if headers is not None and not isinstance(headers, list):
        headers = ', '.join(x.upper() for x in headers)
    # use str instead of basestring if using Python 3.x
    if not isinstance(origin, list):
        origin = ', '.join(origin)
    if isinstance(max_age, timedelta):
        max_age = max_age.total_seconds()

    def get_methods():
        """ Determines which methods are allowed
        """
        if methods is not None:
            return methods

        options_resp = current_app.make_default_options_response()
        return options_resp.headers['allow']

    def decorator(f):
        """The decorator function
        """
        def wrapped_function(*args, **kwargs):
            """Caries out the actual cross domain code
            """
            if automatic_options and request.method == 'OPTIONS':
                resp = current_app.make_default_options_response()
            else:
                resp = make_response(f(*args, **kwargs))
            if not attach_to_all and request.method != 'OPTIONS':
                return resp

            h = resp.headers
            h['Access-Control-Allow-Origin'] = origin
            h['Content-type'] = "application/json"
            h['Access-Control-Allow-Methods'] = get_methods()
            h['Access-Control-Max-Age'] = str(max_age)
            h['Access-Control-Allow-Credentials'] = 'true'
            h['Access-Control-Allow-Headers'] = \
                "Origin, X-Requested-With, Content-Type, Accept, Authorization"
            if headers is not None:
                h['Access-Control-Allow-Headers'] = headers
            return resp

        f.provide_automatic_options = False
        return update_wrapper(wrapped_function, f)
    return decorator



class CustomJsonEncoder(json.JSONEncoder):
    """ Custom Json Encoder that encodes Mensa, weekdays, (...) obj. to JSON """
    def default(self, o):
        # Here you can serialize your object depending of its type
        # or you can define a method in your class which serializes the object
        if isinstance(o, (Mensa, Weekday, MealType, Menu)):
            return o.__dict__  # Or another method to serialize it
        else:
            return json.JSONEncoder.encode(self, o)





@app.route('/api/getMensaForTimespan', methods=['GET', 'OPTIONS'])
@crossdomain(origin = '*')
def getMensaForTimespan():
    """
    ### API Path   `/api/getAllMensas`
    ### Request Type: `GET`
    Returns all mensas for the actual week
    """
    startDay = request.args.get('start')
    endDay = request.args.get('end')


    startTimeDate = datetime.strptime(startDay, '%Y-%m-%d').date()
    endTimeDate = datetime.strptime(endDay, '%Y-%m-%d').date()

    json_data = json.dumps( loadMensaFromDateToDate(mydb, startTimeDate, endTimeDate), cls=CustomJsonEncoder,indent=2, sort_keys=False)
    return json_data, Status.HTTP_OK_BASIC;

@app.route('/api/getMensaForCurrentWeek', methods=['GET', 'OPTIONS'])
@crossdomain(origin = '*')
def getMensaForCurrentWeek():
    """
    ### API Path   `/api/getAllMensas`
    ### Request Type: `GET`
    Returns all mensas for the actual week
    """
    json_data = json.dumps( loadMensaMapForCurrentWeek(mydb), cls=CustomJsonEncoder,indent=2, sort_keys=False)
    return json_data, Status.HTTP_OK_BASIC;



class Mensa:
    def __init__(self, jsonObject):
        self.name = jsonObject["name"]
        self.weekdays = {}
        self.openings = jsonObject["openings"]
        self.category = jsonObject["category"]


    def setWeek(self, date):
        self.loadedWeek = str(date)

    def addMenuFromDb(self, menuDbObject, date, db):
        if(str(date) not in self.weekdays):
            self.weekdays[str(date)] = Weekday(date, "type?", date.strftime("%A"), date.weekday())

        day = self.weekdays[str(date)]
        day.addMenu(menuDbObject, self.name, db)

    def addWeekday(self, weekday):
        for day in self.weekdays:
            if(day.label == weekday.label):
                day.addMealTypeFromDay(weekday)
                return
        self.weekdays.append(weekday)



class Weekday:
    def __init__(self, date, type, weekday, weekdayNumber):
        self.number = weekdayNumber
        self.label = weekday
        self.mealTypes = {}

        self.date = str(date)

    def addMenu(self, menuDbObject, mensaName, db):
        typeName = menuDbObject["mealType"]

        if(typeName not in self.mealTypes):
            self.mealTypes[typeName] = MealType(menuDbObject["mealType"], mensaName, db)

        self.mealTypes[typeName].addMenu(Menu(menuDbObject))


    def addMealTypeFromDay(self, day):
        for mType in day.mealTypes:
            self.mealTypes.append(mType)

class MealType:
    def __init__(self, label, mensa, db):
        self.label = label

        self.hours = {
            "from" : None,
            "to": None
        }
        collection = db["mealtypes"]

        for mealtype in collection.find({"mensa": mensa, "type": label}):
            self.hours = {
                "from" : mealtype["from"],
                "to": mealtype["to"]
            }

        self.menus = []

    def addMenu(self, menu):
        self.menus.append(menu)

class Menu:
    def __init__(self, menuDbObject):
        self.mensa = menuDbObject["mensaName"]
        self.name = menuDbObject["menuName"]
        self.id = menuDbObject["id"]
        self.prices = menuDbObject["prices"]
        self.description = menuDbObject["description"]
        self.isVegi = menuDbObject["isVegi"]
        self.allergene = menuDbObject["allergen"]
        self.date = menuDbObject["date"]




def loadDayIntoMensaMap(date, db, mensaMap):
    """Adds all Menus for the given date to the mensa Map"""
    collection = db["menus"]
    mensa = None

    for menu in collection.find({"date": str(date)}).sort("mensaName"):
        if((mensa is None or mensa.name != menu["mensaName"]) and menu["mensaName"] in mensaMap):
            mensa = mensaMap[menu["mensaName"]]
        mensa.addMenuFromDb(menu, date, db)


def getEmptyMensaMapFromDb(db):
    """ creates an empty mensa map containing empty mensa objects for each menesa"""
    mensaMap = {}
    for mensa in db["mensas"].find():
        mensaMap[mensa["name"]] = Mensa(mensa)

    return mensaMap


def loadMensaFromDateToDate(db, startDate, endDate):
    """ Loads all Menus for the current week into a mensa map [mensaName <=> MensaObject] and returns it"""
    dates = []
    while startDate <= endDate:
        if(startDate.weekday() > 4):
            startDate = startDate + timedelta(days = 1)
            print(startDate)
            continue #Skip weekends

        dates.append(startDate);
        startDate = startDate + timedelta(days = 1)
        print(startDate)
    #
    # dayDiff = startDate.day
    # dates = [startOfWeek + timedelta(days=i) for i in range(5)]
    print(dates)
    return loadMensaMapForGivenDatesFromDb(db, dates , None)


def loadMensaMapForCurrentWeek(db):
    """ Loads all Menus for the current week into a mensa map [mensaName <=> MensaObject] and returns it"""
    today = date.today()

    if(today.weekday() < 5):
        startOfWeek = today - timedelta(days=today.weekday())
    else:
        # It is saturday or sunday, load menus for next weekl
        startOfWeek = today + timedelta(days=-today.weekday())

    dates = [startOfWeek + timedelta(days=i) for i in range(5)]

    return loadMensaMapForGivenDatesFromDb(db, dates , None)


def loadMensaMapForGivenDatesFromDb(db, datesList, mensaMap):
    """ Loads all menus for the given dates inside the given mensamap and returns it. If mensaMap is None a new one will be returned"""
    if(mensaMap is None):
        mensaMap = getEmptyMensaMapFromDb(db)

    for mDate in datesList:
        print("loading menus for date:" + str(mDate))
        loadDayIntoMensaMap(mDate, db, mensaMap)
    return mensaMap
