////////////////////////////////////////////////////////////////////////
//
// ParserStateInsideField.java
//
// This file was generated by MapForce 2007sp1.
//
// YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
// OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
//
// Refer to the MapForce Documentation for further details.
// http://www.altova.com/mapforce
//
////////////////////////////////////////////////////////////////////////

package com.altova.text.tablelike.csv;

class ParserStateInsideField extends ParserState {
    public ParserStateInsideField(Parser owner, ParserStateFactory states) {
        super(owner, states);
    }

    public ParserState process(char current) {
        super.getOwner().appendCharacterToToken(current);
        super.getOwner().moveNext();
        return this;
    }

    public ParserState processFieldDelimiter(char current) {
        super.getOwner().notifyAboutTokenComplete();
        super.getOwner().moveNext();
        return super.getStates().getWaitingForField();
    }

    public ParserState processRecordDelimiter(char current)
            throws BadFormatException {
        super.getOwner().notifyAboutTokenComplete();
        super.getOwner().notifyAboutEndOfRecord();
        super.getOwner().moveNext();
        return super.getStates().getWaitingForField();
    }

    public ParserState processQuoteCharacter(char current) {
        return this.process(current);
    }

}