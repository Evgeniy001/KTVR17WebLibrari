/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import entity.Reader;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Melnikov
 */
@Stateless
public class ReaderFacade extends AbstractFacade<Reader> {

    @PersistenceContext(unitName = "KTVR17WebLibraryPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ReaderFacade() {
        super(Reader.class);
    }

    public Reader fineByLogin(String login) {
       try{
          return (Reader) em.createQuery("SELECT r FROM Reader r WHERE r.login = :login")
                  .setParameter("login", login)
                  .getSingleResult();
       }catch (Exception e){
           return null;
       }
    }
}
