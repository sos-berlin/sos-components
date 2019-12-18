package com.sos.joc.db.calendars;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.calendar.DBItemCalendar;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.UsedBy;
import com.sos.joc.model.tree.Tree; 

public class CalendarsDBLayer {
	
	private SOSHibernateSession session;

    public CalendarsDBLayer(SOSHibernateSession connection) {
        this.session = connection;
    }
    
    public SOSHibernateSession getSession() {
    	return session;
    }

    public DBItemCalendar getCalendar(Long id) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
        	return session.get(DBItemCalendar.class, id);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemCalendar getCalendar(String masterId, String path) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_CALENDARS);
            sql.append(" where schedulerId = :schedulerId");
            sql.append(" and name = :name");
            Query<DBItemCalendar> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", masterId);
            query.setParameter("name", path);
            return session.getSingleResult(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemCalendar renameCalendar(String masterId, String path, String newPath) throws JocException {
        try {
            DBItemCalendar calendarDbItem = getCalendar(masterId, path);
            if (calendarDbItem == null) {
                throw new DBMissingDataException(String.format("calendar '%1$s' not found", path));
            }
            calendarDbItem.setName(newPath);
            Path p = Paths.get(newPath);
            calendarDbItem.setBaseName(p.getFileName().toString());
            calendarDbItem.setDirectory(p.getParent().toString().replace('\\', '/'));
            calendarDbItem.setModified(Date.from(Instant.now()));
            session.update(calendarDbItem);
            return calendarDbItem;
        } catch (JocException ex) {
            throw ex;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<String> getCategories(String masterId) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select category from ").append(DBLayer.DBITEM_CALENDARS);
            sql.append(" where schedulerId = :schedulerId").append(" group by category order by category");
            Query<String> query = session.createQuery(sql.toString());
            query.setParameter("instanceId", masterId);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public DBItemCalendar saveOrUpdateCalendar(String masterId, DBItemCalendar calendarDbItem, Calendar calendar)
    		throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            Date now = Date.from(Instant.now());
            boolean newCalendar = (calendarDbItem == null);
            if (newCalendar) {
                calendarDbItem = new DBItemCalendar();
                calendarDbItem.setSchedulerId(masterId);
                calendarDbItem.setCreated(now);
            }
            if (calendar.getCategory() != null) {
                calendarDbItem.setCategory(calendar.getCategory());
            } else {
                calendarDbItem.setCategory("");
            }
            Path p = Paths.get(calendar.getPath());
            calendarDbItem.setBaseName(p.getFileName().toString());
            calendarDbItem.setDirectory(p.getParent().toString().replace('\\', '/'));
            calendarDbItem.setName(calendar.getPath());
            calendarDbItem.setTitle(calendar.getTitle());
            calendarDbItem.setType(calendar.getType().name());
            calendar.setId(null);
            calendar.setPath(null);
            calendar.setName(null);
			UsedBy usedby = calendar.getUsedBy();
            calendar.setUsedBy(null);
            calendarDbItem.setConfiguration(new ObjectMapper().writeValueAsString(calendar));
            calendarDbItem.setModified(now);
            calendar.setPath(calendarDbItem.getName());
            calendar.setName(calendarDbItem.getBaseName());
			calendar.setUsedBy(usedby);
            if (newCalendar) {
                session.save(calendarDbItem);
            } else {
                calendar.setId(calendarDbItem.getId());
                session.update(calendarDbItem);
            }
            return calendarDbItem;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void deleteCalendars(String masterId, Set<String> paths) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            for (DBItemCalendar calendarDbItem : getCalendarsFromPaths(masterId, paths)) {
                session.delete(calendarDbItem);
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public void deleteCalendar(DBItemCalendar dbCalendar) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (dbCalendar != null) {
                session.delete(dbCalendar);
            }
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemCalendar> getCalendarsFromIds(Set<Long> ids) throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_CALENDARS);
            if (ids != null && !ids.isEmpty()) {
                if (ids.size() == 1) {
                    sql.append(" where id = :id");
                } else {
                    sql.append(" where id in (:id)");
                }
            }
            Query<DBItemCalendar> query = session.createQuery(sql.toString());
            if (ids != null && !ids.isEmpty()) {
                if (ids.size() == 1) {
                    query.setParameter("id", ids.iterator().next());
                } else {
                    query.setParameterList("id", ids);
                }
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemCalendar> getCalendarsFromPaths(String masterId, Set<String> paths) throws DBConnectionRefusedException,
            DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_CALENDARS);
            sql.append(" where schedulerId = :schedulerId");
            if (paths != null && !paths.isEmpty()) {
                if (paths.size() == 1) {
                    sql.append(" and name = :name");
                } else {
                    sql.append(" and name in (:name)");
                }
            }
            Query<DBItemCalendar> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", masterId);
            if (paths != null && !paths.isEmpty()) {
                if (paths.size() == 1) {
                    query.setParameter("name", paths.iterator().next());
                } else {
                    query.setParameterList("name", paths);
                }
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemCalendar> getCalendars(String masterId, String type, Set<String> categories, Set<String> folders,
    		Set<String> recursiveFolders) throws DBConnectionRefusedException, DBInvalidDataException {
        // all recursiveFolders are included in folders too
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_CALENDARS);
            sql.append(" where schedulerId = :schedulerId");
            if (type != null && !type.isEmpty()) {
                sql.append(" and type = :type");
            } else {
                sql.append(" and type in ('WORKING_DAYS','NON_WORKING_DAYS')"); 
            }
            if (categories != null && !categories.isEmpty()) {
                if (categories.size() == 1) {
                    sql.append(" and category = :category");
                } else {
                    sql.append(" and category in (:category)");
                }
            }
            String folder = null;
            if (folders != null && !folders.isEmpty()) {
                if (folders.size() == 1) {
                    folder = folders.iterator().next();
                    if (recursiveFolders != null && recursiveFolders.contains(folder)) {
                        sql.append(" and (directory = :directory or directory like :likeDirectory)");
                    } else {
                        sql.append(" and directory = :directory");
                    }
                } else {
                    if (recursiveFolders != null && !recursiveFolders.isEmpty()) {
                        sql.append(" and (directory in (:directory)");
                        for (int i = 0; i < recursiveFolders.size(); i++) {
                            sql.append(" or directory like :likeDirectory" + i);
                        }
                        sql.append(")");
                    } else {
                        sql.append(" and directory in (:directory)");
                    }
                }
            }
            Query<DBItemCalendar> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", masterId);
            if (type != null && !type.isEmpty()) {
                query.setParameter("type", type.toUpperCase());
            }
            if (categories != null && !categories.isEmpty()) {
                if (categories.size() == 1) {
                    query.setParameter("category", categories.iterator().next());
                } else {
                    query.setParameterList("category", categories);
                }
            }
            if (folders != null && !folders.isEmpty()) {
                if (folders.size() == 1 && folder != null) {
                    query.setParameter("directory", folder);
                    if (recursiveFolders != null && recursiveFolders.contains(folder)) {
                        if (folder.equals("/")) {
                            query.setParameter("likeDirectory", recursiveFolders.iterator().next() + "%");
                        } else {
                            query.setParameter("likeDirectory", recursiveFolders.iterator().next() + "/%");
                        }
                    }
                } else {
                    query.setParameterList("directory", folders);
                    if (recursiveFolders != null && !recursiveFolders.isEmpty()) {
                        int index = 0;
                        for (String recuriveFolder : recursiveFolders) {
                            if (recuriveFolder.equals("/")) {
                                query.setParameter("likeDirectory" + index, recuriveFolder + "%");
                            } else {
                                query.setParameter("likeDirectory" + index, recuriveFolder + "/%");
                            }
                            index++;
                        }
                    }
                }
            }
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public Set<Tree> getFoldersByFolder(String masterId, String folderName, Set<String> types)
    		throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            if (types == null) {
                types = new HashSet<String>();
            }
            if (types.isEmpty()) {
                types.add("WORKING_DAYS");
                types.add("NON_WORKING_DAYS");
            }
            StringBuilder sql = new StringBuilder();
            sql.append("select directory from ").append(DBLayer.DBITEM_CALENDARS);
            sql.append(" where schedulerId = :schedulerId");
            if (types.size() == 1) {
                sql.append(" and type = :type");
            } else {
                sql.append(" and type in (:type)");
            }
            if (folderName != null && !folderName.isEmpty() && !folderName.equals("/")) {
                sql.append(" and ( directory = :folderName or directory like :likeFolderName )");
            }
            sql.append(" group by directory");
            Query<String> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", masterId);
            if (types.size() == 1) {
                query.setParameter("type", types.iterator().next());
            } else {
                query.setParameterList("type", types);
            }
            if (folderName != null && !folderName.isEmpty() && !folderName.equals("/")) {
                query.setParameter("folderName", folderName);
                query.setParameter("likeFolderName", folderName + "/%");
            }
            
            List<String> result = getSession().getResultList(query);
            if (result != null && !result.isEmpty()) {
                return result.stream().map(s -> {
                    Tree tree = new Tree();
                    tree.setPath(s);
                    return tree;
                }).collect(Collectors.toSet());
            }
            return null;
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

    public List<DBItemCalendar> getCalendarsOfAnObject(String masterId, String objectType, String path)
    		throws DBConnectionRefusedException, DBInvalidDataException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select c from ").append(DBLayer.DBITEM_CALENDARS).append(" c, ");
            sql.append(DBLayer.DBITEM_CALENDAR_USAGE).append(" icu ");
            sql.append("where c.id = icu.calendarId ");
            sql.append("and icu.schedulerId = :schedulerId ");
            sql.append("and icu.objectType = :objectType ");
            sql.append("and icu.path = :path");
            Query<DBItemCalendar> query = session.createQuery(sql.toString());
            query.setParameter("schedulerId", masterId);
            query.setParameter("objectType", objectType);
            query.setParameter("path", path);
            return session.getResultList(query);
        } catch (SOSHibernateInvalidSessionException ex) {
            throw new DBConnectionRefusedException(ex);
        } catch (Exception ex) {
            throw new DBInvalidDataException(ex);
        }
    }

}
