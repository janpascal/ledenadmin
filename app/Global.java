import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import models.*;

import com.avaje.ebean.Ebean;

import controllers.Leden;
import controllers.Afschriften;

import play.Application;
import play.GlobalSettings;

public class Global extends GlobalSettings {
    
    @Override
    public void onStart(Application app) {
        InitialData.insert(app);
    }
    
    static class InitialData {
        
        public static void insert(Application app) {
            if(Ebean.find(Lid.class).findRowCount() == 0) {
                System.out.println("Seeding members");
                try {
                    Leden.perform_csvimport(new File("/home/janpascal/huis/bestuur/email-ledenlijst2.csv"));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("Seeding bankafschriften");
                try {
                    Afschriften.perform_csvimport(new File("/home/janpascal/heerlijkrecht/financien vereniging/ING-mutaties-download-2321903_16-02-2012_14-06-2013.csv"));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("Seeding contributiefacturen");
                int aantalFacturen = 0;
                for(int jaar=2009; jaar<=2013; jaar++) {
                    for( Lid lid: Lid.find.all()) {
                        Factuur factuur = new FactuurContributie(new Date(), lid, 20, jaar);
                        factuur.save();
                        aantalFacturen++;
                    }
                }
                System.out.println("Generated "+aantalFacturen+" facturen");
        
                /*
                Afschrift afschrift = new Afschrift(new Date(), (long)2000, Afschrift.AfBij.BIJ, "551242663", "Contributie 2011");
                afschrift.save();
                Factuur factuur = new FactuurContributie(Lid.find.byId((long) 5), (long) 2000, 2011);
                factuur.betaling = afschrift;
                factuur.save();
               */ 
                Lid jp = Lid.find.byId((long) 5);
                jp.addRekening("551242663");
                jp.update(jp.id);
                int count=1;
                for(Lid lid: Lid.find.all()) {
                  lid.addRekening(new Integer(count*5462).toString());
                  lid.update(lid.id);
                  count++;
                  if(count>10) break;
                }
                /*
                System.out.println(jp.getFirstName());
                List<FactuurContributie> facturen = 
                        FactuurContributie.find
                          .where()
                            .eq("jaar",2011)
                            .eq("lid", jp)
                          .findList();
                for(FactuurContributie f: facturen) {
                    System.out.println(f.jaar);
                    if(f.betaling!=null) {
                        System.out.println(f.betaling.mededelingen);
                    }
                }
                */
                
            }
        }
        
    }
    
}


