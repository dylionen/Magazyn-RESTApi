package apteka.functionality;

import apteka.tables.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.List;

public class FixStocks {
    public static void calculateQuantityOfCurrentWHM(int idWhm) {
        SessionFactory sessionArticleFactory = SessionFactories.NewFactory();

        Session session = sessionArticleFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();


        List<WHMList> whmLists = session.createQuery("from WHMList whl where whl.idWHM.bufor = false and whl.idWHM.idWh = "
                + idWhm).getResultList();

        System.out.println("wchodze do funkcji");

        for (WHMList whmElement : whmLists) {
            System.out.println("licze");
            calculateCurrentArticleIdAndLocalization(whmElement.getIdArticle().getIdArticle(),
                    whmElement.getIdWHM().getForeignLocalization());
        }


        session.getTransaction().commit();
    }

    public static void calculateCurrentArticleIdAndLocalization(int idArticle, int idLocalization) {
        SessionFactory sessionArticleFactory = SessionFactories.NewFactory();

        Session session = sessionArticleFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Query sumQueryForPM;
        Query sumQueryForWM;
        double quantityForPM;
        double quantityForWM;

        List<Article> articleList = session.createQuery("from Article where foreignLocalization = " + idLocalization
                + " and idArticle = " + idArticle).getResultList();


        sumQueryForPM = session.createQuery(
                "select SUM(value) from WHMList wl where  idArticle = " + articleList.get(0).getIdArticle() +
                        " and  wl.idWHM.idLocalization = " + idLocalization + " and wl.idWHM.bufor = false and wl.idWHM.idTypeWHM.id_whmtype = 2 ");
        quantityForPM = sumQueryForPM.list().get(0) != null ? Double.valueOf(sumQueryForPM.list().get(0).toString()) : 0.0;

        sumQueryForWM = session.createQuery(
                "select SUM(value) from WHMList wl where  idArticle = " + articleList.get(0).getIdArticle() +
                        " and  wl.idWHM.idLocalization = " + idLocalization + " and wl.idWHM.bufor = false and wl.idWHM.idTypeWHM.id_whmtype = 1 ");
        quantityForWM = sumQueryForWM.list().get(0) != null ? Double.valueOf(sumQueryForWM.list().get(0).toString()) : 0.0;

        articleList.get(0).setQuantity((int) (quantityForPM - quantityForWM));
        System.out.println("PM: " + quantityForPM + "   WM: " + quantityForWM);
        session.getTransaction().commit();
    }

}

