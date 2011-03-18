////////////////////////////////////////////////////////////////////////
//
// TextDocument.java
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

package com.altova.text.edi;

import com.altova.text.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

import com.altova.AltovaException;

public abstract class TextDocument extends Parser {
		
	private Generator mGenerator = new Generator();
	private String mStructureName = "";
	private String mEncoding = "";
	private Particle mRootParticle;

	protected TextDocument (Particle rootParticle) {
			this.mRootParticle = rootParticle;
	}
	
	public String getEncoding() {
		return mEncoding;
	}
	
	public void setEncoding(String encoding) {
		mEncoding = encoding;
	}
	
	private StringBuffer loadBufferFromString(String str) {
		StringBuffer result;
		try {
			result = FileIO.readToEnd(str, mEncoding);
			
		} catch (IOException x) {
			throw new AltovaException(x);
		}
		int i = 0;
		while (Character.isWhitespace(result.charAt(i)))
			++i;
		if (i > 0)
			result.delete(0, i - 1);
		return result;
	}
	
	private StringBuffer loadBufferFromFile(String filename) {
		StringBuffer result;
		try {
			FileIO io = new FileIO(filename, mEncoding);
			result = io.readToEnd();
		} catch (IOException x) {
			throw new AltovaException(x);
		}
		int i = 0;
		while (Character.isWhitespace(result.charAt(i)))
			++i;
		if (i > 0)
			result.delete(0, i - 1);
		return result;
	}

	public Generator getGenerator() {
		return mGenerator;
	}

	public String getStructureName() {
		return mStructureName;
	}
	public String saveToString() throws Exception {
		
		TextNode root = mGenerator.getRootNode();

		if (getSettings().getAutoCompleteData()) {
			DataCompletion datacompletion = null;
			
			switch (getEDIKind()) {
			case EDIFact:
				datacompletion = new EDIFactDataCompletion((EDIFactSettings) getSettings(), mStructureName);
				break;
			case EDIX12:
				datacompletion = new EDIX12DataCompletion((EDIX12Settings) getSettings(), mStructureName);
				break;
			}
			datacompletion.completeData(root, mRootParticle);
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OutputStreamWriter writer = FileIO.openByteArrayWriteStream(bos, mEncoding);
		
		if (EDIFact == getEDIKind()) {
			EDIFactSettings edifactsettings = (EDIFactSettings) getSettings();
			if (edifactsettings.getWriteUNA()) {
				getSettings().getServiceChars().serialize(writer);
				if (getSettings().getTerminateSegmentsWithLinefeed())
					writer.write("\r\n");
			}
		}
		
		Writer ediWriter = new Writer(writer, getSettings().getServiceChars());
		ediWriter.setNewlineAfterSegments(getSettings().getTerminateSegmentsWithLinefeed());
		
		mRootParticle.getNode().write(ediWriter, root);
		writer.flush();
		String results = bos.toString(mEncoding);
		writer.close();
		return results;
	}
	public void setStructureName(String rhs) {
		mStructureName = rhs;
	}

	public ITextNode parseFile(String filename) throws Exception {
		StringBuffer buffer = loadBufferFromFile(filename);
		if (validateSource(buffer)) {
			super.parse(mRootParticle, buffer.toString(), mGenerator, getSettings().getServiceChars());
			mGenerator.resetToRoot();
			validateResult();
			return mGenerator.getRootNode();
		}
		return NullTextNode.getInstance();
	}

	public ITextNode parseString(String input) throws Exception {
		StringBuffer buffer = loadBufferFromString(input);
		if (validateSource(buffer)) {
			super.parse(mRootParticle, buffer.toString(), mGenerator, getSettings().getServiceChars());
			mGenerator.resetToRoot();
			validateResult();
			return mGenerator.getRootNode();
		}
		return NullTextNode.getInstance();
	}
	final protected static short EDIFact = 1;

	final protected static short EDIX12 = 2;

	protected abstract short getEDIKind();

	protected abstract boolean validateSource(StringBuffer source);

	protected boolean validateResult() {
		this.removeEmptyNodes(mGenerator.getRootNode());
		return true;
	}

	protected abstract EDISettings getSettings();
		
	public void save(String filename) throws Exception {
		
		TextNode root = mGenerator.getRootNode();

		if (getSettings().getAutoCompleteData()) {
			DataCompletion datacompletion = null;
			
			switch (getEDIKind()) {
			case EDIFact:
				datacompletion = new EDIFactDataCompletion((EDIFactSettings) getSettings(), mStructureName);
				break;
			case EDIX12:
				datacompletion = new EDIX12DataCompletion((EDIX12Settings) getSettings(), mStructureName);
				break;
			}
			datacompletion.completeData(root, mRootParticle);
		}
		
		FileIO io = new FileIO (filename, mEncoding);
		OutputStreamWriter writer = io.openWriteStream();
		if (EDIFact == getEDIKind()) {
			EDIFactSettings edifactsettings = (EDIFactSettings) getSettings();
			if (edifactsettings.getWriteUNA()) {
				getSettings().getServiceChars().serialize(writer);
				if (getSettings().getTerminateSegmentsWithLinefeed())
					writer.write("\r\n");
			}
		}
		
		Writer ediWriter = new Writer(writer, getSettings().getServiceChars());
		ediWriter.setNewlineAfterSegments(getSettings().getTerminateSegmentsWithLinefeed());
		
		mRootParticle.getNode().write(ediWriter, root);
		writer.close();
	}

	private boolean isEmptyDataElement(ITextNode node) {
		return ((ITextNode.DataElement == node.getNodeClass()) && (0 == node
				.getValue().length()));
	}

	private boolean isNodeContainerWithoutChildren(ITextNode node) {
		byte nodeClass = node.getNodeClass();
		return (nodeClass == ITextNode.Composite ||
				nodeClass == ITextNode.Group) &&
				(node.getChildren().size() == 0);
	}

	private void removeEmptyNodes(ITextNode node) {
		int i = 0;
		while (i < node.getChildren().size()) {
			ITextNode kid = node.getChildren().getAt(i);
			removeEmptyNodes(kid);
			if ((isEmptyDataElement(kid))
					|| (isNodeContainerWithoutChildren(kid)))
				node.getChildren().removeAt(i);
			else
				++i;
		}
	}
}
