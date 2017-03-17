package com.codebrig.jvmmechanic.agent.event;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public enum MechanicEventType {

    ENTER_EVENT,
    EXIT_EVENT,
    BEGIN_WORK_EVENT,
    END_WORK_EVENT,
    CORRUPT_EVENT;

    public byte toEventTypeId() {
        return (byte) ordinal();
    }

}
