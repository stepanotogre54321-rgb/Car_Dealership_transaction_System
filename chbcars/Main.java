package chbcars;

import chbcars.data.DataStore;
import chbcars.ui.LoginFrame;
import chbcars.ui.Theme;

public class Main {

    public static void main(String[] args) {
      
            DataStore.get().initialize(); 
            Theme.install();
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
          
            }
        ;
    }

