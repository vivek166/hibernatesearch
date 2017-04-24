package com.synerzip.textsearch;
 
import java.util.List;
import java.util.Scanner;
 
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
 
/**
 * Hello world!
 * 
 */
public class App {
     
    private static void doIndex() throws InterruptedException {
        Session session = HibernateUtil.getSession();
         
        FullTextSession fullTextSession = Search.getFullTextSession(session);
        fullTextSession.createIndexer().startAndWait();
         
        fullTextSession.close();
    }
     
    private static List<Contact> search(String queryString) {
        Session session = HibernateUtil.getSession();
        FullTextSession fullTextSession = Search.getFullTextSession(session);
         
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Contact.class).get();
        org.apache.lucene.search.Query luceneQuery = queryBuilder.keyword().onFields("name").matching(queryString).createQuery();
 
        // wrap Lucene query in a javax.persistence.Query
        org.hibernate.Query fullTextQuery = fullTextSession.createFullTextQuery(luceneQuery, Contact.class);
         
        List<Contact> contactList = fullTextQuery.list();
         
        fullTextSession.close();
         
        return contactList;
    }
     
    private static void displayContactTableData() {
        Session session = null;
         
        try {
            session = HibernateUtil.getSession();
             
            // Fetching saved data
            List<Contact> contactList = session.createQuery("from Contact").list();
             
            for (Contact contact : contactList) {
                System.out.println(contact);
            }
             
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally{
            if(session != null) {
                session.close();
            }
        }
    }
     
    public static void main(String[] args) throws InterruptedException {
        System.out.println("\n\n******Data stored in Contact table******\n");
        displayContactTableData();
         
        // Create an initial Lucene index for the data already present in the database
        doIndex();
         
        Scanner scanner = new Scanner(System.in);
        String consoleInput = null;
         
        while (true) {
            // Prompt the user to enter query string
            System.out.print("\n\nEnter search key (To exit type 'X')");            
            consoleInput = scanner.nextLine();
             
            if("X".equalsIgnoreCase(consoleInput)) {
                System.out.println("End");
                System.exit(0);
            }   
             
            List<Contact> result = search(consoleInput);            
            System.out.println("\n\n>>>>>>Record found for '" + consoleInput + "'");
             
            for (Contact contact : result) {
                System.out.println(contact);
            }               
        }           
    }
}