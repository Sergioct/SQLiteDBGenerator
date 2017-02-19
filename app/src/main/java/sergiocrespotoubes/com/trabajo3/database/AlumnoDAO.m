#import "AlumnoDAO.h"
#import "Alumno.h"

@implementation AlumnoDAO

static Alumno *instance;

+ (Alumno *) instance {
	if(instance == nil){
		instance = [[Alumno alloc] init];
	}
	return instance;
}

- (NSMutableArray *) getAll{

	const char *sql =  "SELECT * FROM Alumno";

	NSMutableArray * list = [[NSMutableArray alloc] init];

	Alumno *item = [[Alumno alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.nombre = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 0)];
			item.dni = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 1)];
			item.nota = sqlite3_column_int(sqlStatement, 2);
			item.chico = sqlite3_column_int(sqlStatement, 3);
		[list addObject:item];
		}
		sqlite3_finalize(sqlStatement);
		sqlite3_close(db);
	}
	return list;
}

- (Alumno *) getById:(NSInteger)auxid{

	const char *sql =  [[NSString stringWithFormat:@"SELECT * FROM Alumno where id=%ld",auxid] UTF8String];

	Alumno *item = [[Alumno alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.nombre = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 0)];
			item.dni = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 1)];
			item.nota = sqlite3_column_int(sqlStatement, 2);
			item.chico = sqlite3_column_int(sqlStatement, 3);
		}
		sqlite3_finalize(sqlStatement);
		sqlite3_close(db);
	}
	return item;
}

-(void)createObject:(Alumno *)item {
	NSString *sqlInsert = [NSString stringWithFormat:@"Insert into Alumno(NOMBRE, DNI, NOTA, CHICO) VALUES ('%@', '%@', '%@', '%ld')", item.nombre, item.dni, item.nota, (long)item.chico];


	const char *sql = [sqlInsert UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

- (void) updateObject:(Alumno*)item{


	const char *sql = [[NSString stringWithFormat:@"update Alumno nombre = %@, dni = %@, nota = %ld, chico = %ld where id=%ld" , item.nombre, item.dni, item.nota, item.chico, item.myid] UTF8String];

	sqlite3_stmt *sqlStatement;

	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}

}

- (void) deleteObject:(NSInteger)auxid{

	const char *sql = [[NSString stringWithFormat:@"delete from Alumno where myid=%ld",auxid] UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

@end