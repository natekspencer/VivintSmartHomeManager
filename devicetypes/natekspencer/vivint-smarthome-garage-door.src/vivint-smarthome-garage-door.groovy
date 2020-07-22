/**
 *  vivint.SmartHome Garage Door
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

metadata {
    definition (name: "vivint.SmartHome Garage Door", namespace: "natekspencer", author: "Nathan Spencer") {
        capability "Door Control"
        capability "Refresh"
        capability "Health Check"
        capability "Actuator"
        capability "Sensor"
    }
    
    preferences {
    }

    tiles(scale: 2) {
        standardTile("door", "device.door", width: 2, height: 2, decoration: "flat") {
            state "closed" , label: 'closed' , action: "open" , nextState: "opening", backgroundColor: "#00a0dc", icon: "st.doors.garage.garage-closed" , defaultState: true
            state "opening", label: 'opening', action: "open" , nextState: "opening", backgroundColor: "#ffffff", icon: "st.doors.garage.garage-opening"
            state "open"   , label: 'open'   , action: "close", nextState: "closing", backgroundColor: "#ffffff", icon: "st.doors.garage.garage-open"
            state "closing", label: 'closing', action: "close", nextState: "closing", backgroundColor: "#00a0dc", icon: "st.doors.garage.garage-closing"
            state "unknown", label: 'unknown',                                        backgroundColor: "#ff0000", icon: ""
            state "unknown", label: 'unknown',                                        backgroundColor: "#ff0000", icon: ""
        }

        standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
            state "refresh", label: 'refresh', action: "refresh", icon: "st.secondary.refresh"
        }
        
        main("door")
        details(["door", "refresh"])
    }
}

def installed() {
}

def updated() {
}

def close() {
    doOpen(false)
}

def open() {
    doOpen(true)
}

def doOpen(open) {
    def dni = device.deviceNetworkId.tokenize("."),
        value = open ? 4 : 2,
        status = open ? "opening" : "closing"
    parent.dispatchCommand(dni[0], dni[1], "door", ["s": value])
    sendEvent(name: "door", value: status)
    runIn(25, refresh)
}

def refresh() {
    def dni = device.deviceNetworkId.tokenize(".")
    parseEventData(parent.getDeviceData(dni[0], dni[1]))
}

def parseEventData(Map results) {
    results.each {name, value ->
        switch (name) {
            case "ol":
                sendEvent(name: "DeviceWatch-DeviceStatus", value: value ? "online" : "offline", displayed: false)
                sendEvent(name: "healthStatus", value: value ? "online" : "offline", displayed: false)
                break
            case "s":
                def status = "unknown"
                switch (value) {
                    case 1:
                        status = "closed"
                        break
                    case 2:
                        status = "closing"
                        break
                    case 3: // stopped, but treat as open since the door will report open anyway
                        status = "open"
                        break
                    case 4:
                        status = "opening"
                        break
                    case 5:
                        status = "open"
                        break
                    case 0:
                    default:
                        status = "unknown"
                        break
                }
                sendEvent(name: "door", value: status)
                break
        }
    }
}