////////////////////////////////////////////////////////////////////////
//
// StructureItem.java
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

import com.altova.text.ITextNode;
import com.altova.text.ITextNodeList;
import com.altova.text.NullTextNode;
import java.io.IOException;

public abstract class StructureItem
{
	protected byte mNodeClass;
	protected String mName;
	protected Particle[] mChildren;

	public byte getNodeClass() {
		return mNodeClass;
	}

	protected boolean readChildren (Parser.Context context, byte separator)
	{
		Scanner scanner = context.getScanner();

		for (int childIndex = 0; childIndex != getChildCount(); ++childIndex)
		{
			if (childIndex != 0 && separator != ServiceChars.None && scanner.isAtSeparator(separator))
				scanner.rawConsumeChar();

			Particle currentParticle = mChildren[childIndex];
			int toRead = currentParticle.getMergedEntries();
			byte repSeparator = separator;
			if (toRead == 1) // no merged entry
			{
				toRead = Integer.MAX_VALUE; // try to read as much as possible -> errors are reported anyways
				if (mNodeClass == ITextNode.Segment)
				{
					repSeparator = ServiceChars.RepetitionSeparator;
					if (scanner.getServiceChars().getRepetitionSeparator() == '\0')
					{
						toRead = 1;
						repSeparator = ServiceChars.None;
					}
				}
				else if (currentParticle.getMaxOccurs() == 1)
				{
					toRead = 1;
					repSeparator = ServiceChars.None;
				}
			}

			Parser.Context childContext = context.newContext(context, currentParticle);
			for (int count = 0; count < toRead; ++count)
			{
				// consume the proper separator. otherwise the children won't find anything to read and fail anyways.
				if (count != 0 && repSeparator != ServiceChars.None  &&scanner.isAtSeparator(repSeparator))
					scanner.rawConsumeChar ();

				//Scanner.State preservedState = scanner.getCurrentState();
				if (!currentParticle.getNode().read (childContext))
				{
					//scanner.setCurrentState(preservedState);
					if (count >= currentParticle.getMinOccurs())
					{
						if (count >= currentParticle.getMergedEntries())
							break; // enough read
					}
					else
					{
						if (mNodeClass == ITextNode.Group)
						{
							childContext.handleError (Parser.MissingSegmentOrGroup);
							return false;
						}
						else
							childContext.handleError (Parser.MissingFieldOrComposite);
					}
				}

				if (currentParticle.getMergedEntries() == 1 && count >= currentParticle.getMaxOccurs())
					childContext.handleError (Parser.ExtraRepeat);
				if (repSeparator != ServiceChars.None)
				{
					if (scanner.consumeString(repSeparator, true).toString().length() > 0)
						childContext.handleError(Parser.ExtraData);
				}
			}
		}
		return true;
	}

	protected void writeChildren (Writer writer, ITextNode node, byte separator) throws IOException {
		int structChildren = getChildCount();
		int childPos = 0;

		for ( ; childPos < structChildren; ++childPos)
		{
			Particle currentParticle = mChildren[childPos];
			int nToWrite = currentParticle.getMergedEntries();
			byte actSeparator = separator;
			if (currentParticle.getMaxOccurs() > 1 || getNodeClass() == ITextNode.Group)
			{
				//	be tolerant for maxOccurs overruns, but don't eat all nodes for non-repeating items:
				nToWrite = Integer.MAX_VALUE;

				if (getNodeClass() == ITextNode.Segment)
				{
					actSeparator = ServiceChars.RepetitionSeparator;
					if (writer.getServiceChars().getSeparator(actSeparator) == '\0')
						nToWrite = 1; // no separator -> need to suppress extra repetitions
				}
			}

			ITextNodeList children = node.getChildren().filterByName(currentParticle.getNameOverride());

			for (int nCount = 0; nCount < nToWrite; ++nCount)
			{
				if (nCount < children.size())
					currentParticle.getNode().write(writer, children.getAt(nCount));
				else
				{
					if (nCount < currentParticle.getMinOccurs())
					{
						if (getNodeClass() == ITextNode.Group)
							currentParticle.getNode().write(writer, NullTextNode.getInstance());
					}
					else
						if (nCount >= currentParticle.getMergedEntries())
							break;
				}
				writer.addSeparator(actSeparator);
			}
			if (actSeparator != separator)
			{
				writer.clearPendingSeparators(actSeparator);
				writer.addSeparator(separator);
			}
		}
		writer.clearPendingSeparators(separator);
	}



	protected boolean isAtGroup (Parser.Context context)
	{
		StructureItem node = this;
		Particle particle = node.child(0);
		while (particle.getNode().getNodeClass() == ITextNode.Group)
		{
			node = particle.getNode();
			particle = node.child(0);
		}

		int nIndex = 0;
		// for the special Interchange and Envelope groups different behavior is needed.
		while (nIndex != node.getChildCount())
		{
			StructureItem child = particle.getNode();
			if (child.getNodeClass() == ITextNode.Segment)
			{
				// try to find out whether this segment appears here.
				Scanner.State preservedState = context.getScanner().getCurrentState();
				boolean result = child.isSegmentStarting (context);
				context.getScanner().setCurrentState(preservedState);
				if (result)
					return true;
			}
			else // shouldn't be anything else.
			{
				if (child.isAtGroup (context))
					return true;
			}

			// the segment is mandatory -> the group cannot occur here.
			if (particle.getMinOccurs() > 0)
				return false;
			++nIndex;
			particle = node.child(nIndex);
		}
		// this could happen in cases where groups have no indicator segment.
		return false;
	}

	protected boolean isSegmentStarting (Parser.Context context)
	{
		Scanner scanner = context.getScanner();
		scanner.skipWhitespace(); // skip whitespace before/between segments

		if (mName.equals("UNA") || mName.equals("ISA"))
		{
			// check segment tag - character by character because separators are not known yet
			for (int i = 0; i < 3; ++i)
				if (scanner.rawConsumeChar() != mName.charAt(i))
					return false;
		}
		else
		{
			// check segment tag
			String sTag = scanner.consumeString(ServiceChars.ComponentSeparator, true).toString();
			if ( !sTag.equals(mName))
				return false;
		}
		return true;
	}

	public abstract boolean read(Parser.Context context);

	public abstract void write(Writer writer, ITextNode node) throws IOException;

	public String getName() {
		return mName;
	}

	public Particle child(int i) {
		return mChildren[i];
	}

	public int getChildCount() {
		return (mChildren == null) ? 0:mChildren.length;
	}

	protected StructureItem (String name, byte cls) {
		this.mName = name;
		this.mChildren = null;
		this.mNodeClass = cls;
	}

	protected StructureItem (String name, byte cls, Particle[] children) {
		this.mName = name;
		this.mChildren = children;
		this.mNodeClass = cls;
	}
}
