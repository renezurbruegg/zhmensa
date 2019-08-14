# !/usr/bin/env python
# -*- coding: utf-8 -*-
"""This module has configurations for flask app."""

import logging
import os
from datetime import timedelta
CONFIG = {
    "development": "flask_app.config.DevelopmentConfig",
    "testing": "flask_app.config.TestingConfig",
    "production": "flask_app.config.ProductionConfig",
    "default": "flask_app.config.ProductionConfig"
}


class BaseConfig(object):
    """Base class for default set of configs."""

    POLLING_TIME_DELAY = 0.1
    PORT = 8080
    IP = '0.0.0.0'
    PIN_CODE = "1234"
    DEBUG = False
    TESTING = False
    SECURITY_PASSWORD_HASH = 'pbkdf2_sha512'
    SECURITY_TRACKABLE = True
    LOGGING_FORMAT = "[%(asctime)s] [%(funcName)-30s] +\
                                    [%(levelname)-6s] %(message)s"
    LOGGING_LOCATION = 'web.log'
    LOGGING_LEVEL = logging.DEBUG
    SECURITY_TOKEN_MAX_AGE = 60 * 30
    SECURITY_CONFIRMABLE = False
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    CACHE_TYPE = 'simple'
    SECURITY_PASSWORD_SALT = 'super-secret-stuff-here'
    COMPRESS_MIMETYPES = ['text/html', 'text/css', 'text/xml',
                          'application/json', 'application/javascript']

    WTF_CSRF_ENABLED = False
    COMPRESS_LEVEL = 6
    COMPRESS_MIN_SIZE = 500

    LOG_TO_CONSOLE = False
    JWT_EXPIRES = timedelta(minutes=10)


class DevelopmentConfig(BaseConfig):
    """Default set of configurations for development mode."""

    DEBUG = False
    LOG_TO_CONSOLE = True
    TESTING = False
    BASEDIR = os.path.abspath(os.path.dirname(__file__))
    SECRET_KEY = 'not-so-super-secret'
    JWT_SECRET_KEY = 'another_super_awesome_secret_stuff_yo.'


class ProductionConfig(BaseConfig):
    """Default set of configurations for prod mode."""

    DEBUG = False
    TESTING = False
    BASEDIR = os.path.abspath(os.path.dirname(__file__))

    SECRET_KEY = 'lG8-PNVN-hft6-7S2n-BAyhC-FALO-G1by-aipw'
    JWT_SECRET_KEY = '2RsKj89-DCZowzk-QwB5g3-t0wBW4Q-wticR.'


class TestingConfig(BaseConfig):
    """Default set of configurations for test mode."""

    DEBUG = False
    TESTING = True
    SECRET_KEY = '792842bc-c4df-4de1-9177-we2vgns23sd'
    JWT_SECRET_KEY = 'XhCF-W3oT-iSuP-jKmr-ETSuJ-WkYPJ-tlKAxe.'
