/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package secure;

import entity.Reader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import session.ReaderFacade;
import session.RoleFacade;
import session.UserRolesFacade;
import util.EncriptPass;
import util.PageReturner;

/**
 *
 * @author Melnikov
 */
@WebServlet(loadOnStartup = 1,name = "Secure", urlPatterns = {
    "/login",
    "/logout",
    "/showLogin",
    "/editUserRoles",
    "/changeUserRole"
})
public class Secure extends HttpServlet {
   
    @EJB RoleFacade roleFacade;
    @EJB ReaderFacade readerFacade;
    @EJB UserRolesFacade userRolesFacade;

    @Override
    public void init() throws ServletException {
        List<Reader> listReader = readerFacade.findAll();
        if(listReader.isEmpty()){
            EncriptPass ep = new EncriptPass();
            String salts = ep.createSalts();
            String encriptPass = ep.setEncriptPass("admin", salts);
            Reader reader = new Reader("Сидор", "Сидоров", 
                 "454545454", "К-Ярве", "admin", encriptPass, salts);
            readerFacade.create(reader);
            Role role = new Role();
            role.setName("ADMIN");
            roleFacade.create(role);
            UserRoles ur = new UserRoles();
            ur.setReader(reader);
            ur.setRole(role);
            userRolesFacade.create(ur);
            role.setName("USER");
            roleFacade.create(role);
            ur.setReader(reader);
            ur.setRole(role);
            userRolesFacade.create(ur);
        }
    }
    
    
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF8");
        HttpSession session = request.getSession(false);
        Reader regUser = null;
        if(session != null){
            try {
                regUser = (Reader) session.getAttribute("regUser");
            } catch (Exception e) {
                regUser = null;
            }
        }
            
        SecureLogic sl = new SecureLogic();
        String path = request.getServletPath();
        if(null != path)
            switch (path) {
        case "/login":
            String login = request.getParameter("login");
            String password = request.getParameter("password");
            request.setAttribute("info", "Нет такого пользователя!");
            regUser = readerFacade.fineByLogin(login);
            if(regUser == null){
                request.getRequestDispatcher(PageReturner.getPage("showLogin"))
                    .forward(request, response);
                break;
            }
            EncriptPass ep = new EncriptPass();
            String salts = regUser.getSalts();
            String encriptPass = ep.setEncriptPass(password, salts);
            if(encriptPass.equals(regUser.getPassword())){
                session = request.getSession(true);
                session.setAttribute("regUser", regUser);
                request.setAttribute("info", "Привет "+regUser.getName()
                        +"! Вы вошли в систему.");
                request.getRequestDispatcher(PageReturner.getPage("welcome"))
                        .forward(request, response);
                break;
            }
            request.getRequestDispatcher(PageReturner.getPage("showLogin"))
                    .forward(request, response);
            break;
        case "/showLogin":
            request.getRequestDispatcher(PageReturner.getPage("showLogin"))
                    .forward(request, response);
            break;
        case "/logout":
            if(session != null){
                session.invalidate();
                request.setAttribute("info", "Вы вышли из системы");
            }
            request.getRequestDispatcher(PageReturner.getPage("welcome"))
                    .forward(request, response);
            break;
        case "/editUserRoles":
            if(!"ADMIN".equals(sl.getRole(regUser))){
                request.getRequestDispatcher(PageReturner.getPage("showLogin"))
                    .forward(request, response);
                break;
            }
            
            Map<Reader,String> mapUsers = new HashMap<>();
            List<Reader> listUsers = readerFacade.findAll();
            int n = listUsers.size();
            for(int i=0;i<n;i++){
                mapUsers.put(listUsers.get(i), sl.getRole(listUsers.get(i)));
            }
            List<Role> listRoles = roleFacade.findAll();
            request.setAttribute("mapUsers", mapUsers);
            request.setAttribute("listRoles", listRoles);
            request.getRequestDispatcher(PageReturner.getPage("editUserRoles"))
                    .forward(request, response);
            break;
        case "/changeUserRole":
            if(!"ADMIN".equals(sl.getRole(regUser))){
                request.getRequestDispatcher(PageReturner.getPage("showLogin"))
                    .forward(request, response);
                break;
            }
            String setButton = request.getParameter("setButton");
            String deleteButton = request.getParameter("deleteButton");
            String userId = request.getParameter("user");
            String roleId = request.getParameter("role");
            Reader reader = readerFacade.find(new Long(userId));
            Role roleToUser = roleFacade.find(new Long(roleId));
            UserRoles ur = new UserRoles(reader, roleToUser);
            if(setButton != null){
                sl.addRoleToUser(ur);
            }
            if(deleteButton != null){
                sl.deleteRoleToUser(ur.getReader());
            }
            mapUsers = new HashMap<>();
            listUsers = readerFacade.findAll();   
            n = listUsers.size();
            for(int i=0;i<n;i++){
                mapUsers.put(listUsers.get(i), sl.getRole(listUsers.get(i)));
            }
            request.setAttribute("mapUsers", mapUsers);
            List<Role> newListRoles = roleFacade.findAll();
            request.setAttribute("listRoles", newListRoles);
            request.getRequestDispatcher(PageReturner.getPage("editUserRoles"))
                    .forward(request, response);
            break;
            }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}