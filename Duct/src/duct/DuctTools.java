/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package duct;

/**
 *
 * @author rnagel
 */
public abstract class DuctTools
{    
    public static boolean SHOW_OUTPUT = true;
    public static boolean SHOW_ERRORS = true;
    public static boolean SHOW_POPUPS = true;

    public static void print(String text)
    {
        if (SHOW_OUTPUT)
        {
            System.out.print(text);
        }
    }
    
    public static void printLine(String text)
    {
        if (SHOW_OUTPUT)
        {
            System.out.println(text);
        }
    }
    
    public static void showError(Exception ex)
    {   
        if (SHOW_ERRORS)
        {
            String msg = ex.toString().replace(":", ":\n");
            StringBuilder builder = new StringBuilder();
            int cCnt = 0;
            for (int c = 0; c < msg.length(); c++)
            {
                if (cCnt > 80 && Character.isWhitespace(msg.charAt(c)))
                {
                    builder.append("\n");
                    cCnt = 0;
                }
                else if (msg.charAt(c) == '\n')
                {
                    builder.append(msg.charAt(c));
                    cCnt = 0;
                }
                else
                {
                    builder.append(msg.charAt(c));
                    cCnt++;
                }            
            }
            System.err.println("JUST Error: " + ex.getClass().getCanonicalName());
            System.err.println(builder.toString());
        }
    }
}