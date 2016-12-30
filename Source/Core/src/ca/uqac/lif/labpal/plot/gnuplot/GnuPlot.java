/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2017 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.labpal.plot.gnuplot;

import java.util.Calendar;

import ca.uqac.lif.labpal.CommandRunner;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.table.Table;

/**
 * Top-level class for plots drawn using the GnuPlot software.
 * @author Sylvain Hallé
 */
public abstract class GnuPlot extends Plot
{
	/**
	 * The symbol used to separate data values in a file
	 */
	public static final transient String s_datafileSeparator = ",";
	
	/**
	 * The symbol used to represent missing values in a file
	 */
	public static final transient String s_datafileMissing = "?";
	
	/**
	 * The path to launch GnuPlot
	 */
	protected static transient String s_path = "gnuplot";

	/**
	 * Whether gnuplot is present on the system
	 */
	protected static transient final boolean s_gnuplotPresent = FileHelper.commandExists(s_path);
	
	/**
	 * The fill style used to draw the graph
	 */
	protected transient FillStyle m_fillStyle = FillStyle.SOLID;
	
	/**
	 * The fill style used for the plot
	 */
	public static enum FillStyle {SOLID, NONE, PATTERN};


	/**
	 * The time to wait before polling GnuPlot's result
	 */
	protected static transient long s_waitInterval = 100;
	
	public GnuPlot(Table table)
	{
		super(table);
	}
	
	/**
	 * Generates a stand-alone Gnuplot file for this plot
	 * @param term The terminal used to display the plot
	 * @return The Gnuplot file contents
	 */
	public final String toGnuplot(ImageType term)
	{
		return toGnuplot(term, "");
	}

	/**
	 * Generates a stand-alone Gnuplot file for this plot
	 * @param term The terminal used to display the plot
	 * @param lab_title The title of the lab. This is only used in the 
	 *   auto-generated comments in the file's header
	 * @return The Gnuplot file contents
	 */
	public abstract String toGnuplot(ImageType term, String lab_title);
	
	public final byte[] getImage(ImageType term)
	{
		String instructions = toGnuplot(term);
		byte[] image = null;
		String[] command = {s_path};
		CommandRunner runner = new CommandRunner(command, instructions);
		runner.start();
		// Wait until the command is done
		while (runner.isAlive())
		{
			// Wait 0.1 s and check again
			try
			{
				Thread.sleep(s_waitInterval);
			}
			catch (InterruptedException e)
			{
				// This happens if the user cancels the command manually
				runner.stopCommand();
				runner.interrupt();
				return null;
			}
		}
		image = runner.getBytes();
		if (image == null || image.length == 0)
		{
			// Gnuplot could not produce a picture; return the blank image
			image = s_blankImage;
		}
		return image;		
	}

	/**
	 * Checks if Gnuplot is present in the system
	 * @return true if Gnuplot is present, false otherwise
	 */
	public static boolean isGnuplotPresent()
	{
		return s_gnuplotPresent;
	}
	
	/**
	 * Gets a GnuPlot terminal name from an image type
	 * @param t The image type
	 * @return The terminal name
	 */
	public static String getTerminalName(ImageType t)
	{
		switch (t)
		{
		case PNG:
			return "png";
		case PDF:
			return "pdf";
		case DUMB:
			return "dumb";
		case CACA:
			return "caca";
		}
		return "dumb";
	}

	
	/**
	 * Produces a header that is common to all plots generated by the
	 * application
	 * @param term The terminal to display this plot
	 * @return The header
	 */
	public StringBuilder getHeader(ImageType term, String lab_name)
	{
		StringBuilder out = new StringBuilder();
		out.append("# ----------------------------------------------------------------").append(FileHelper.CRLF);
		out.append("# File generated by ParkBench ").append(Laboratory.s_versionString).append(FileHelper.CRLF);
		out.append("# Date:     ").append(String.format("%1$te-%1$tm-%1$tY", Calendar.getInstance())).append(FileHelper.CRLF);
		out.append("# Lab name: ").append(lab_name).append(FileHelper.CRLF);
		out.append("# ----------------------------------------------------------------").append(FileHelper.CRLF);
		out.append("set title \"").append(m_title).append("\"").append(FileHelper.CRLF);
		out.append("set datafile separator \"").append(s_datafileSeparator).append("\"").append(FileHelper.CRLF);
		out.append("set datafile missing \"").append(s_datafileMissing).append("\"").append(FileHelper.CRLF);
		out.append("set terminal ").append(getTerminalName(term)).append(FileHelper.CRLF);
		switch (m_fillStyle)
		{
		case PATTERN:
			out.append("set style fill pattern").append(FileHelper.CRLF);
			break;
		case SOLID:
			out.append("set style fill solid").append(FileHelper.CRLF);
			break;
		default:
			// Do nothing
		}
		return out;
	}
	
	/**
	 * Gets the fill color associated with a number, based on the palette
	 * defined for this plot.
	 * @param color_nb The color number
	 * @return An empty string if no palette is defined, otherwise the
	 *   <tt>fillcolor</tt> expression corresponding to the color
	 */
	protected final String getFillColor(int color_nb)
	{
		if (m_palette == null || m_fillStyle != FillStyle.SOLID)
		{
			return "";
		}
		return "fillcolor rgb \"" + m_palette.getHexColor(color_nb) + "\"";
	}

}
