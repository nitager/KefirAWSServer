package com.pr.server.services;
import java.awt.Color;
import java.awt.image.BufferedImage;


public class ImageConverter 
{
	public static BufferedImage reverseImage(BufferedImage image)
	{
		for (int i = 0; i < image.getWidth(); i++)
		{
			for (int j = 0; j < image.getHeight(); j++)
			{
				Color color = new Color(image.getRGB(i, j));
				
				int red, green, blue;
				red = 255 - color.getRed();
				green = 255 - color.getGreen();
				blue = 255 - color.getBlue();
				color = new Color(red,green,blue);
				
				image.setRGB(i, j, color.getRGB());
			}
		}
		image.flush();
		return image;
	}
}
