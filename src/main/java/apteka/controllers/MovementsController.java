package apteka.controllers;

import apteka.functionality.AuthValidator;
import apteka.functionality.CodersClass;
import apteka.functionality.FixStocks;
import apteka.functionality.PDFCreator;
import apteka.tables.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@RestController
public class MovementsController {
    SessionFactory sessionFactory = new Configuration()
            .configure("hibernate.cfg.xml")
            .addAnnotatedClass(WHM.class)
            .addAnnotatedClass(WHMList.class)
            .addAnnotatedClass(UserType.class)
            .addAnnotatedClass(VATTable.class)
            .addAnnotatedClass(User.class)
            .addAnnotatedClass(TypesWHM.class)
            .addAnnotatedClass(Article.class)
            .addAnnotatedClass(Unit.class)
            .addAnnotatedClass(Localization.class)
            .addAnnotatedClass(Address.class)
            .addAnnotatedClass(AddressType.class)
            .addAnnotatedClass(Contact.class)
            .addAnnotatedClass(ContactType.class)
            .addAnnotatedClass(ArticleReport.class)
            .buildSessionFactory();
    ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/createWM")
    public int createWM(@RequestHeader(value = "Authorization") String authId,
                        @RequestHeader(value = "Localization") int localization,
                        @RequestBody WHM jsonWHM) {
        System.out.println(jsonWHM.getForeignName());
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();

        User user = AuthValidator.getAuth(authId);
        if (user == null) return -1;

        List<TypesWHM> typesWHMS = session.createQuery("from TypesWHM where id_whmtype = 1").getResultList();
        List<Localization> listLocalizations = session.createQuery("from Localization where idLocalization = " + localization).getResultList();

        WHM whm = new WHM(user, typesWHMS.get(0), jsonWHM.getPrice(), jsonWHM.isBufor(), jsonWHM.getPriceB(),
                jsonWHM.getForeignName(), listLocalizations.get(0), new Date());
        session.save(whm);
        session.getTransaction().commit();

        if (!jsonWHM.isBufor()) {
            FixStocks.calculateQuantityOfCurrentWHM(whm.getIdWh());
        }

        return whm.getIdWh();
    }

    @GetMapping("/getWM")
    public String getWM(@RequestHeader(value = "Authorization") String authId, @RequestHeader(value = "Localization") int localization) throws JsonProcessingException {
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();
        List<WHM> WH = null;

        try {
            WH = session.createQuery("from WHM where idTypeWHM = 1 and idLocalization = " + localization + " order by createdDate DESC ").getResultList();
            System.out.println(WH);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            return e.getMessage();
        } finally {
            //session.getTransaction().commit();
        }
        return objectMapper.writeValueAsString(WH);
    }

    @PostMapping("/createPM")
    public int createPM(@RequestHeader(value = "Authorization") String authId,
                        @RequestHeader(value = "Localization") int localization,
                        @RequestBody WHM jsonWHM) {

        System.out.println(jsonWHM.getForeignName());
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();

        User user = AuthValidator.getAuth(authId);
        if (user == null) return -1;

        List<TypesWHM> typesWHMS = session.createQuery("from TypesWHM where id_whmtype = 2").getResultList();
        List<Localization> listLocalizations = session.createQuery("from Localization where idLocalization = " + localization).getResultList();

        WHM whm = new WHM(user, typesWHMS.get(0), jsonWHM.getPrice(), jsonWHM.isBufor(), jsonWHM.getPriceB(),
                jsonWHM.getForeignName(), listLocalizations.get(0), new Date());
        session.save(whm);
        session.getTransaction().commit();

        if (!jsonWHM.isBufor()) {
            FixStocks.calculateQuantityOfCurrentWHM(whm.getIdWh());
        }

        return whm.getIdWh();
    }

    @GetMapping("/getPM")
    public String getPM(@RequestHeader(value = "Authorization") String authId, @RequestHeader(value = "Localization") int localization) throws JsonProcessingException {
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();
        List<WHM> WH = null;

        try {
            WH = session.createQuery("from WHM where idTypeWHM = 2  and idLocalization = " + localization + " order by createdDate DESC ").getResultList();
            System.out.println(WH);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            return e.getMessage();
        } finally {
            //session.getTransaction().commit();
        }
        return objectMapper.writeValueAsString(WH);
    }


    @GetMapping("/getPM/{PMid}")
    public String getPMCurrent(@PathVariable int PMid, @RequestHeader(value = "Authorization") String authId) throws JsonProcessingException {
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();
        List<WHM> WH = null;

        try {
            WH = session.createQuery("from WHM where idWh =" + PMid).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            return e.getMessage();
        } finally {
            //session.getTransaction().commit();
        }
        return objectMapper.writeValueAsString(WH.get(0));
    }


    @GetMapping("getWMArticlesList/{WHMid}")
    public String getWMArticlesList(@PathVariable int WHMid, @RequestHeader(value = "Authorization") String authId) throws JsonProcessingException {
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();
        List<WHMList> whmLists;

        try {
            whmLists = session.createQuery("from WHMList where id_warehouse_movement =" + WHMid).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            return e.getMessage();
        } finally {
            //session.getTransaction().commit();
        }
        return objectMapper.writeValueAsString(whmLists);
    }


    @PostMapping("/createWMArticlesList/{WHMid}")
    public ResponseEntity createWMArticlesList(@PathVariable int WHMid, @RequestHeader(value = "Authorization") String authId,
                                               @RequestBody List<WHMList> whmList, @RequestHeader(value = "Localization") int localization)
            throws JsonProcessingException {
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();

        List<WHM> whm = session.createQuery("from WHM where idWh =" + WHMid).getResultList();

        List<WHMList> whmNewList = new LinkedList<>();
        WHMList WHMListOne = null;

        List<Article> articles;
        List<VATTable> vat;

        System.out.println(Arrays.toString(whmList.toArray()));


        for (WHMList whTmp : whmList) {

            //Article idArticle, WHM idWHM, double value, VATTable idVATTable, double price


            articles = session.createQuery("from Article where idArticle=" + whTmp.getForeignIdArticle()).getResultList();
            vat = session.createQuery("from VATTable where idVat = " + whTmp.getForeignIdVATTable()).getResultList();


            WHMListOne = new WHMList(articles.get(0), whm.get(0), whTmp.getValue(), vat.get(0), whTmp.getPrice(), whTmp.getPriceB());
            whmNewList.add(WHMListOne);
            session.save(WHMListOne);

            //Chwilowo brak zasotosowania
            //FixStocks.calculateQuantity(whTmp.getForeignIdArticle(),localization);
        }

        tx.commit();

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/getVAT")
    public String createWMArticlesList(@RequestHeader(value = "Authorization") String authId) throws JsonProcessingException {
        Session session = sessionFactory.getCurrentSession();
        List<VATTable> vatTables = null;

        Transaction tx = session.beginTransaction();

        try {
            vatTables = session.createQuery("from VATTable").getResultList();

        } catch (Exception e) {
            tx.rollback();
        } finally {
            session.getTransaction().commit();
            session.close();
            //session.getTransaction().commit();
        }
        return objectMapper.writeValueAsString(vatTables);
    }


    @PutMapping("/confirmPW/{WHMid}")
    public ResponseEntity confirmPw(@PathVariable int WHMid) throws JsonProcessingException {

        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();
        List<WHM> whmList = session.createQuery("from WHM where idWh = " + WHMid).getResultList();
        if (whmList.size() != 1) {
            return new ResponseEntity<>(
                    "Brak dokumentu o przesłanych parametrach.",
                    HttpStatus.BAD_REQUEST);
        }
        WHM whm = whmList.get(0);
        whm.setBufor(false);


        session.getTransaction().commit();
        session.close();
        FixStocks.calculateQuantityOfCurrentWHM(whm.getIdWh());

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/countArticle/{ArticleId}")
    public String countArticle(@RequestHeader(value = "Localization") int localization,
                               @RequestHeader(value = "Authorization") String authId, @PathVariable String ArticleId) {
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();

        User user = AuthValidator.getAuth(authId);
        if (user == null) return "Nieprawidlowe";


        System.out.println(localization);

        Query sumQuery = session.createQuery(
                "select SUM(value) from WHMList wl where idArticle = " + ArticleId + " and  wl.idWHM.idLocalization = " + localization);
        Double ilosc = sumQuery.list().get(0) != null ? Double.valueOf(sumQuery.list().get(0).toString()) : 0.0;
        session.getTransaction().commit();
        session.close();


        return ilosc.toString();
    }

    @GetMapping("/getWHMDocumentsWithCurrentArticle/{idArticle}")
    public String getWHMDocumentsWithCurrentArticle(@RequestHeader(value = "Authorization") String authId, @RequestHeader(value = "Localization")
            int localization, @PathVariable int idArticle) throws JsonProcessingException {

        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();

        User user = AuthValidator.getAuth(authId);
        if (user == null) return "Auth failed";

        List<WHMList> whmList;


        try {
            whmList = session.createQuery("from WHMList wh where wh.idWHM.bufor = false and idArticle = " + idArticle).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println(e.getMessage());
            return e.getMessage();
        } finally {
            session.close();
        }
        return objectMapper.writeValueAsString(whmList);
    }

    @GetMapping("/getArticleReportWithCurrentArticle/{idArticle}")
    public String getArticleReportWithCurrentArticle(@RequestHeader(value = "Authorization") String authId, @RequestHeader(value = "Localization")
            int localization, @PathVariable int idArticle) throws JsonProcessingException {

        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();

        User user = AuthValidator.getAuth(authId);
        if (user == null) return "Auth failed";

        List<ArticleReport> articleReports;

        //  PDFCreator pdfCreator;  //for test

        try {
            articleReports = session.createQuery("from ArticleReport ar where ar.idArticle.idArticle = " + idArticle).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            return e.getMessage();
        } finally {
            session.close();
        }
        return objectMapper.writeValueAsString(articleReports);
    }

    @PostMapping("/createArticleReport/{idArticle}")
    public ResponseEntity createArticleReport(@RequestHeader(value = "Authorization") String authId, @PathVariable String idArticle, @RequestHeader(value = "Localization")
            int localization, @RequestBody String date) {

        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();
        PDFCreator pdfCreator;

        User user = AuthValidator.getAuth(authId);
        if (user == null) return new ResponseEntity<String>("Auth failed", HttpStatus.NOT_ACCEPTABLE);

        List<Article> articlesList = session.createQuery("from Article where idArticle= " + idArticle).getResultList();
        if (articlesList.size() == 0) return new ResponseEntity<String>("Article Error", HttpStatus.NOT_ACCEPTABLE);

        List<Localization> localizationList = session.createQuery("from Localization where idLocalization= " + localization).getResultList();
        if (localizationList.size() == 0)
            return new ResponseEntity<String>("Localization Error", HttpStatus.NOT_ACCEPTABLE);

        List<WHMList> whmList = session.createQuery("from WHMList wh where wh.idWHM.bufor = false and idArticle = " + idArticle +
                " and wh.idWHM.createdDate < '" + CodersClass.changeFormatDate(date) + "'").getResultList();
        if (whmList.size() == 0) return ResponseEntity.ok(HttpStatus.OK);
        ArticleReport articleReport;

        pdfCreator = new PDFCreator(whmList, date);
        articleReport = new ArticleReport(articlesList.get(0), localizationList.get(0), user,
                pdfCreator.getDocumentByte(), CodersClass.changeFormatDate(date));

        try {
            session.save(articleReport);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            return new ResponseEntity<String>(e.getMessage().toString(), HttpStatus.NOT_ACCEPTABLE);
        } finally {
            session.close();
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/reports/report/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable int id) {
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();

        List<ArticleReport> articleReports = session.createQuery("from ArticleReport where  idReport = " + id).getResultList();
        tx.commit();
        session.close();
        byte[] document = articleReports.get(0).getDocument();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(document);
    }

    @DeleteMapping("/deleteArticleReport/{id}")
    public ResponseEntity deleteReport(@PathVariable int id) {
        Session session = sessionFactory.getCurrentSession();
        Transaction tx = session.beginTransaction();

        List<ArticleReport> articleReports = session.createQuery("from ArticleReport where  idReport = " + id).getResultList();
        try {
            session.delete(articleReports.get(0));
        } catch (NullPointerException n) {
            ResponseEntity.badRequest();
        } finally {
            tx.commit();
            session.close();
        }

        return ResponseEntity.ok(HttpStatus.OK);
    }

}
