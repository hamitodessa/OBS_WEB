package com.hamit.obs.service.server;

import org.springframework.stereotype.Service;

import com.hamit.obs.createnewDB.createMSSQL;
import com.hamit.obs.createnewDB.createMYSQL;
import com.hamit.obs.createnewDB.createPGSQL;
import com.hamit.obs.dto.server.serverBilgiDTO;

@Service
public class ServerService {

	
	public boolean serverKontrol(serverBilgiDTO serverBilgiDTO)
	{
		boolean result = false;
		if(serverBilgiDTO.getHangi_sql().equals("MS SQL"))
        {
        	createMSSQL createMSSQL = new createMSSQL();
        	result = createMSSQL.serverKontrol(serverBilgiDTO);
        }
		else if(serverBilgiDTO.getHangi_sql().equals("MY SQL"))
        {
        	createMYSQL createMYSQL = new createMYSQL();
        	result = createMYSQL.serverKontrol(serverBilgiDTO);
        }
		else if(serverBilgiDTO.getHangi_sql().equals("PG SQL"))
        {
        	createPGSQL createPGSQL = new createPGSQL();
        	result = createPGSQL.serverKontrol(serverBilgiDTO);
        }
		return result;
	}

	
	public boolean dosyakontrol(serverBilgiDTO serverBilgiDTO)
	{
		boolean result =false;
		if(serverBilgiDTO.getHangi_sql().equals("MS SQL"))
		{
			createMSSQL createMSSQL = new createMSSQL();
			result = createMSSQL.dosyaKontrol(serverBilgiDTO);
		}
		else if(serverBilgiDTO.getHangi_sql().equals("MY SQL"))
		{
			createMYSQL createMYSQL = new createMYSQL();
			result = createMYSQL.dosyaKontrol(serverBilgiDTO);
		}
		else if(serverBilgiDTO.getHangi_sql().equals("PG SQL"))
		{
			createPGSQL createPGSQL = new createPGSQL();
			result = createPGSQL.dosyaKontrol(serverBilgiDTO);
		}
		return result;
	}

	public boolean dosyaolustur(serverBilgiDTO serverBilgiDTO)
	{
		boolean result =false;
		if(serverBilgiDTO.getHangi_sql().equals("MS SQL"))
        {
        	createMSSQL createMSSQL = new createMSSQL();
        	result = createMSSQL.dosyaOlustur(serverBilgiDTO);
        }
		else if(serverBilgiDTO.getHangi_sql().equals("MY SQL"))
        {
        	createMYSQL createMYSQL = new createMYSQL();
        	result = createMYSQL.dosyaOlustur(serverBilgiDTO);
        }
		else if(serverBilgiDTO.getHangi_sql().equals("PG SQL"))
        {
        	createPGSQL createPGSQL = new createPGSQL();
        	result = createPGSQL.dosyaOlustur(serverBilgiDTO);
        }
		return result;
	}
}