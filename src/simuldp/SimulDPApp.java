/*
 * SimulDPApp.java
 */

package simuldp;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import simulacion.ControlSim;
/**
 * The main class of the application.
 */
public class SimulDPApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {             
        ControlSim control=new ControlSim();        
        show(new SimulDPView(this, control));        
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {       
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of SimulDPApp
     */
    public static SimulDPApp getApplication() {
        return Application.getInstance(SimulDPApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(SimulDPApp.class, args);
    }
}
