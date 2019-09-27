@objc
public class BleConnectionEvent: NSObject {

    @objc
    static public let connectingEvent = "ConnectingEvent"

    @objc
    static public let connectedEvent = "ConnectedEvent"

    @objc
    static public let events = [
        connectingEvent,
        connectedEvent
    ]
}
