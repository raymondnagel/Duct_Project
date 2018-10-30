//*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ducttest;
//
//import duct.DuctTools;
//import duct.DuctContext;
//
///**
// *
// * @author raymond.nagel
// */
//public class DuctTest {
//
//    private static DuctContext[] contexts = new DuctContext[4];
//    private static Robot[] robots = new Robot[4];
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//        
//        for (int d = 0; d < robots.length; d++)
//        {
//            robots[d] = new Robot("I am #" + (d+1));
//            
//            contexts[d] = new DuctContext();
//            contexts[d].registerDuctClass(DuctTools.class);     
//            contexts[d].evaluateJavascript("function doIt(){ robot.output(); }");
//            contexts[d].registerObject("robot", robots[d]);
//        }
//          
//        
//        for (int t = 0; t < 10; t++)
//        {
//            for (int d = 0; d < robots.length; d++)
//            {
//                contexts[d].evaluateJavascript("doIt();");
//            }
//        }
//        
//        
//        /*
//        context.evaluateJavascript("for (i=0; i<10; i++) { doIt(); } ");
//        
//        
//
//        */
//    }
//    
//}