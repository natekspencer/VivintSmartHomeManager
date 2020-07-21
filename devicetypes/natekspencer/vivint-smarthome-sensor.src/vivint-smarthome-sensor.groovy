/**
 *  vivint.SmartHome Sensor
 *
 *  Copyright 2020 Nathan Spencer
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  CHANGE HISTORY
 *  VERSION     DATE            NOTES
 *  1.0.0       2020-07-21      Initial release
 *
 */

import groovy.time.TimeCategory

metadata {
    definition (name: "vivint.SmartHome Sensor", namespace: "natekspencer", author: "Nathan Spencer") {
        capability "Contact Sensor"
        capability "Battery"
        capability "Tamper Alert"
        capability "Refresh"
        capability "Sensor"
    }
    
    preferences {
    }

    tiles(scale: 2) {
        standardTile("contact", "device.contact", width: 2, height: 2, decoration: "flat") {
            state "closed", label: 'closed', backgroundColor: "#00a0dc", icon: "st.contact.contact.closed", defaultState: true
            state "open"  , label: 'open'  , backgroundColor: "#ffffff", icon: "st.contact.contact.open"
        }

        valueTile("battery", "device.battery", width: 2, height: 2, decoration: "flat") {
            state "battery", label:'${currentValue}% battery'
        }

        valueTile("tamper", "device.tamper", width: 2, height: 2, decoration: "flat") {
            state "tamper", label:'${currentValue}'
        }
        
        standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
            state "refresh", label: 'refresh', action: "refresh", icon: "st.secondary.refresh"
        }
        
        main("contact")
        details(["contact", "battery", "tamper", "refresh"])
    }
}

def installed() {
}

def updated() {
}

def refresh() {
    def dni = device.deviceNetworkId.tokenize(".")
    parseEventData(parent.getDeviceData(dni[0], dni[1]))
}

def parseEventData(Map results) {
    results.each {name, value ->
        switch (name) {
            case "bl":
                sendEvent(name: "battery", value: value)
                break
            case "s":
                sendEvent(name: "contact", value: value ? "open" : "closed")
                break
            case "ta":
                sendEvent(name: "tamper", value: value ? "detected" : "clear")
                break
        }
    }
}