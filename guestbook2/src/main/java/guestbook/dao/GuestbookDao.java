package guestbook.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import guestbook.vo.GuestbookVo;

public class GuestbookDao {
	
	private Connection getConnection() throws SQLException {
		Connection conn = null;
		
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			
			String url = "jdbc:mariadb://192.168.64.3:3306/webdb?charset=utf8";
			conn = DriverManager.getConnection(url, "webdb", "webdb");
		} catch (ClassNotFoundException e) {
			System.out.println("드라이버 로딩 실패: " + e);
		}
		
		return conn;
	}
	
	public void insert(GuestbookVo mockGuestbookVo) {
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
		
		try {
			conn = getConnection();
			
			pstmt1 = conn.prepareStatement("update guestbook_log set count = count + 1 where date = current_date()");
			pstmt2 = conn.prepareStatement("insert into guestbook_log values(current_date, 1)");
			pstmt3 = conn.prepareStatement("insert into guestbook(name, password, contents, reg_date) values(?, ?, ?, now())");
			pstmt3.setString(1, mockGuestbookVo.getName());
			pstmt3.setString(2, mockGuestbookVo.getPassword());
			pstmt3.setString(3, mockGuestbookVo.getContents());
			
			// Transaction: BEGIN //////////// 
			conn.setAutoCommit(false);
			
			// DML1
			int rowCount = pstmt1.executeUpdate();
			
			// DML2
			if (rowCount == 0) {
				pstmt2.executeUpdate();
			}
			
			// DML3
			pstmt3.executeUpdate();
			
			// Transaction: END(SUCCESS) //////////// 
			conn.commit();
			
		} catch (SQLException e) {
			System.out.println("error: " + e);
			
			// Transaction: END(FAIL) ////////////
			try {
				if (conn != null) {
					conn.rollback();
				}
			} catch (SQLException ignored) {
			}
			
		} finally {
			try {
				if (pstmt3 != null) {
					pstmt3.close();
				}
				if (pstmt2 != null) {
					pstmt2.close();
				}
				if (pstmt1 != null) {
					pstmt1.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ignored) {
			}
		}
	}
	
	public List<GuestbookVo> findAll() {
		List<GuestbookVo> result = new ArrayList<>();
		
		try (
				Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement("select no, name, date_format(reg_date, '%Y/%m/%d %H:%i:%s'), contents from guestbook order by reg_date desc");
				ResultSet rs = pstmt.executeQuery();
		) {
			while (rs.next()) {
				Long no = rs.getLong(1);
				String name = rs.getString(2);
				String reg_date = rs.getString(3);
				String contents = rs.getString(4);
				
				GuestbookVo guestbookVo = new GuestbookVo();
				guestbookVo.setNo(no);
				guestbookVo.setName(name);
				guestbookVo.setContents(contents.replace("\n", "<br>"));
				guestbookVo.setReg_date(reg_date);
				
				result.add(guestbookVo);
			}
		} catch (SQLException e) {
			System.out.println("error: " + e);
		}
		
		return result;
	}

	public void deleteByNoAndPassword(Long no, String password) {
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		
		try {
			conn = getConnection();
			pstmt1 = conn.prepareStatement("update guestbook_log set count = count - 1 where date = (select date(reg_date) from guestbook where no = ?)");
			pstmt1.setLong(1, no);
			
			pstmt2 = conn.prepareStatement("delete from guestbook where no = ? and password = ?");
			pstmt2.setLong(1, no);
			pstmt2.setString(2, password);
			
			// Transaction: BEGIN //////////// 
			conn.setAutoCommit(false);
			
			// DML1
			pstmt1.executeUpdate();
			
			// DML2
			pstmt2.executeUpdate();
			
			// Transaction: END(SUCCESS) //////////// 
			conn.commit();
			
		} catch (SQLException e) {
			System.out.println("error: " + e);
			
			// Transaction: END(FAIL) ////////////
			try {
				if (conn != null) {
					conn.rollback();
				}
			} catch (SQLException ignored) {
			}
			
		} finally {
			try {
				if (pstmt2 != null) {
					pstmt2.close();
				}
				if (pstmt1 != null) {
					pstmt1.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ignored) {
			}
		}
	}
}
