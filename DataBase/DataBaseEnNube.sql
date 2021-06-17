drop database if exists Torrent;
create database Torrent;
use Torrent;

/*
CREATE USER 'WebApplication'@'database-bt.ctazi4dvrctb.us-east-1.rds.amazonaws.com' IDENTIFIED WITH mysql_native_password BY '123456';
ALTER USER 'BDTorrent'@'database-bt.ctazi4dvrctb.us-east-1.rds.amazonaws.com' IDENTIFIED WITH mysql_native_password BY 'HermanosConvoy';
*/

SET GLOBAL log_bin_trust_function_creators = 1;

DROP TABLE IF EXISTS Peers;
CREATE TABLE Peers (
Id_Peer int AUTO_INCREMENT ,
Ip varchar(20),
PuertoEntrada integer,
primary key (Id_Peer),
unique (Ip)
);

DROP TABLE IF EXISTS Archivos;
CREATE TABLE Archivos (
Id_Archivo varchar(100),
Peso int,
Total_Piezas int, 
primary key (Id_Archivo)
);

DROP TABLE IF EXISTS Piezas;
CREATE TABLE Piezas (
Id_Pieza int,
Id_Archivo varchar(100), 
foreign key (Id_Archivo) references Archivos(Id_Archivo),
primary key (Id_Pieza)
);

DROP TABLE IF EXISTS PeerPieza;
CREATE TABLE PeerPieza (
Id_PeerPieza int AUTO_INCREMENT,
Id_Peer int,
Id_Pieza int,
Id_Archivo varchar(100), 
Peso int,
foreign key (Id_Peer) references Peers(Id_Peer),
foreign key (Id_Archivo) references Archivos(Id_Archivo),
foreign key (Id_Pieza) references Piezas(Id_Pieza),
primary key (Id_PeerPieza)
);

DROP TABLE IF EXISTS PeerArchivo;
CREATE TABLE PeerArchivo (
Id_PeerArchivo int AUTO_INCREMENT,
Id_Peer int,
Id_Archivo varchar(100),
Carga int ,
foreign key (Id_Peer) references Peers(Id_Peer),
foreign key (Id_Archivo) references Archivos(Id_Archivo),
primary key (Id_PeerArchivo)
);


delimiter $$
DROP trigger IF EXISTS Progreso;
CREATE TRIGGER Progreso
    AFTER UPDATE
    ON PeerPieza FOR EACH ROW
BEGIN
	set @Piezas= (select count(*) from PeerPieza where Peso>0 and Id_Peer=old.Id_Peer and Id_Archivo=old.Id_Archivo);
    set @Total= (select Total_Piezas from Archivos where Id_Archivo=old.Id_Archivo);
    update PeerArchivo set Carga=(@Piezas/@Total)*100 where Id_Archivo=old.Id_Archivo and Id_Peer=old.Id_Peer;
END$$    
delimiter ;

delimiter $$
DROP PROCEDURE IF EXISTS RegistrarArchivo;
CREATE PROCEDURE RegistrarArchivo (IN IPimp varchar(20) , IN Id_Archivo varchar(100), IN Peso int)
       BEGIN
       set @Cantidad_Piezas = (Peso/(512*1024));
       set @IdPeer = (select Id_Peer from Peers where Ip = IPimp group by Ip);
       insert into Archivos values(Id_Archivo, Peso, @Cantidad_Piezas);
       insert into PeerArchivo values(Null, @IdPeer, Id_Archivo, 100);
       set @contador = 0 ;
       while @contador <= @Cantidad_Piezas Do
			insert into Piezas values(@contador,Id_Archivo);
            insert into PeerPieza values(null, @IdPeer, @contador, Id_Archivo,1);
			set @contador = @contador+1;
       End while;
       END$$
delimiter ;

/*---------------------------------------------------------Archivo para compartir--------------------------------------------------------------------*/
delimiter $$
DROP PROCEDURE IF EXISTS RegistrarArchivoVacio;
CREATE PROCEDURE RegistrarArchivoVacio (IN IPimp varchar(20) , IN Id_Archivo varchar(100), IN Peso int)
       BEGIN
       set @Cantidad_Piezas = (Peso/(512*1024));
       set @IdPeer = (select Id_Peer from Peers where Ip = IPimp group by Ip);
       insert into PeerArchivo values(Null, @IdPeer, Id_Archivo, 0);
       set @contador = 1 ;
       while @contador <= @Cantidad_Piezas Do
            insert into PeerPieza values(null, @IdPeer, @contador, Id_Archivo,0);
			set @contador = @contador+1;
       End while;
       END$$
delimiter ;
/*---------------------------------------------------------Recuperar Fragmentos--------------------------------------------------------------------*/
/*create view RecuperarFragmentosFaltantes as 
select 
 PeerPieza.Id_PeerPieza,
 PeerPieza.Id_Archivo as "Archivo",
 PeerPieza.Peso, 
 Peers.IP as "IP", 
 Peers.PuertoEntrada as "PIn"
from PeerPieza 
inner join Peers ON
Peers.Id_Peer= PeerArchivo.Id_Peer
inner join PeerPieza on
PeerPieza.Id_Archivo=PeerArchivo.Id_Archivo
where Peso>0
Group by IP;
*/
/*insert into Peers values (null,"255.255.1.1","5100");*/
/*call RegistrarArchivoVacio ("255.255.255.255","Goya.mp3",8000000);*/
/*select Id_Peer from Peers where Ip = "255.255.2665.255" group by Ip*/
/*call RegistrarArchivoVacio("255.255.1.255","Joya.mp4",8000000);*/
select Id_Peer from Peers where Ip = "255.255.2665.255" group by Ip;
select Id_Peer from Peers where Ip = "255.255.2665.255" group by Id_Peer;

create view PeersConFragmentosFaltantes as 
select 
 PeerPieza.Id_PeerPieza,
 PeerPieza.Id_Archivo as "Archivo",
 PeerPieza.Peso, 
 Peers.IP as "IP", 
 Peers.PuertoEntrada as "PIn"
from PeerArchivo 
inner join Peers ON
Peers.Id_Peer= PeerArchivo.Id_Peer
inner join PeerPieza on
PeerPieza.Id_Archivo=PeerArchivo.Id_Archivo
where Peso=0
Group by IP; 

Select Ip,Pin from PeersConFragmentosFaltantes where Archivo="Joya.mp4" and Carga=0; /*Caso 1.**/

alter view PeersConFragmentos as 
select 
 PeerPieza.Id_PeerPieza,
 PeerPieza.Id_Archivo as "Archivo",
 PeerPieza.Peso, 
 Peers.IP as "IP", 
 Peers.PuertoEntrada as "PIn",
 PeerArchivo.Carga
from PeerArchivo 
inner join Peers ON
Peers.Id_Peer= PeerArchivo.Id_Peer
inner join PeerPieza on
PeerPieza.Id_Archivo=PeerArchivo.Id_Archivo
where Peso>0 and carga=100
Group by IP; 

select Ip, PIn from PeersConFragmentos where Archivo="Joya.mp4" and carga=100;

delete from Peers;

/*insert into Peers values (null,"255.255.2665.255","5000");*/
/*call RegistrarArchivo ("255.255.1.1","Joya.mp4",8000000);*/
/*select Id_Peer from Peers where Ip = "255.255.2665.255" group by Ip*/
/*call RegistrarArchivoVacio("255.255.255.255","Joya.mp4",8000000);*/

select * from Peers;
select * from Archivos;
select * from PeerArchivo;
select * from PeerPieza;
select* from Piezas;


select Id_Peer from Peers where Ip = "255.255.255.1" group by Ip;
select Ip,PuertoEntrada from Peers where IP='560456';
select Ip,PIn from PeersConFragmentos where Archivo='Joya.mp4';

TRUNCATE TABLE PeerPieza;
truncate table PeerArchivo;
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE Peers;
TRUNCATE TABLE Archivos;
truncate table Piezas;
SET FOREIGN_KEY_CHECKS=1;


UPDATE PeerPieza SET Peso = 1 WHERE Id_Peer =2  and Id_Pieza =8 and Id_Archivo ="Joya.mp4" ;
select * from PeerPieza;

select Carga from PeerArchivo where Id_Peer='' and Id_Archivo='Joya.mp4';




delimiter $$
DROP PROCEDURE IF EXISTS RecuperarArchivos;
CREATE PROCEDURE RecuperarArchivos (IN IPIN varchar(20))
       BEGIN
		set @IdPeer = (select Id_Peer from Peers where Ip = IPIN group by Ip);
		select Id_Archivo,Id_Peer from PeerArchivo where Id_Peer=@IdPeer and Carga<100 group by Id_Archivo;
       END$$
delimiter ;



alter view RecuperarPiezas as 
       select Id_Peer,Id_Pieza,Id_Archivo 
       from PeerPieza where Peso=0 ;

	select *from RecuperarPiezas where Id_Peer=1 and Id_Archivo='Joya.mp4';
    select * from RecuperarPiezas where Id_Peer= 2 and Id_Archivo='Joya.mp4';
    
    select * from ConseguirIPsConFragmentos where Pieza=3 and Archivo='Joya.mp4';
       /*Agregar la condicional el IDPeer Propio con los archivos que nos faltan*/
       /*and Id_Peer=IdPeerIN and Id_Archivo=Id_ArchivoIN;*/

create view ConseguirIPsConFragmentos as 
		select 
        PeerPieza.Id_Peer as "Id",
        PeerPieza.Id_Pieza as "Pieza",
        PeerPieza.Id_Archivo as "Archivo",
        Peers.Ip as "IP",
        Peers.PuertoEntrada as "Puerto"
        from PeerPieza 
        inner join Peers on
        Peers.Id_Peer=PeerPieza.Id_Peer
        where PeerPieza.Peso=1;        
        /*Agregar a la condicional La pieza buscada, el archivo buscado*/




/*
select 
    Peers.IP as "IP",
    Peers.PIn as "Puerto",
    PeerPieza.Id_Archivo as "Archivo",
    PeerPieza.Id_Pieza as "Pieza"
    from Peers 
    inner join  PeerPieza ON 
    Peers.Id_Peer=PeerPieza.Id_Peer;
  */  
    
    
delimiter $$
DROP PROCEDURE IF EXISTS RegistrarSeeder;
CREATE PROCEDURE RegistrarSeeder (IN IPimp varchar(20) , IN Id_Archivo varchar(100), IN Peso int)
       BEGIN
       set @Cantidad_Piezas = (Peso/(512*1024));
       set @IdPeer = (select Id_Peer from Peers where Ip = IPimp group by Ip);
       insert into PeerArchivo values(Null, @IdPeer, Id_Archivo, 100);
       set @contador = 0 ;
       while @contador <= @Cantidad_Piezas Do
            insert into PeerPieza values(null, @IdPeer, @contador, Id_Archivo,1);
			set @contador = @contador+1;
       End while;
       END$$
delimiter ;

call RegistrarSeeder ("172.31.4.90","Joya.mp4",7864320);
    
    
    