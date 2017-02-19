#import "AlumnoDAO.h"
#import "Alumno.h"

@implementation AlumnoDAO

static AlumnoDAO *instance;

+ (AlumnoDAO *) instance {
	if(instance == nil){
		instance = [[AlumnoDAO alloc] init];
	}
	return instance;
}

- (id)init {
	if ((self = [super init])) {
		NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString* documentsDirectory = [paths lastObject];
		NSString* databasePath = [documentsDirectory stringByAppendingPathComponent:@"database.sqlite3"];
		if (sqlite3_open([sqLiteDb UTF8String], &db) != SQLITE_OK) {
			NSLog(@"Failed to open database!");
		}
	}
	return self;
}

- (NSMutableArray *) getAll{

	const char *sql =  "SELECT * FROM Alumno";

	NSMutableArray * list = [[NSMutableArray alloc] init];

	Alumno *item = [[Alumno alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.myid = sqlite3_column_int(sqlStatement, 0);
			item.nombre = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 1)];
			item.dni = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 2)];
			item.nota = sqlite3_column_int(sqlStatement, 3);
			item.chico = sqlite3_column_int(sqlStatement, 4);
		[list addObject:item];
		}
		sqlite3_finalize(sqlStatement);
	}
	return list;
}

- (Alumno *) getById:(NSInteger)auxid{

	const char *sql =  [[NSString stringWithFormat:@"SELECT * FROM Alumno where id=%ld",auxid] UTF8String];

	Alumno *item = [[Alumno alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.myid = sqlite3_column_int(sqlStatement, 0);
			item.nombre = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 1)];
			item.dni = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 2)];
			item.nota = sqlite3_column_int(sqlStatement, 3);
			item.chico = sqlite3_column_int(sqlStatement, 4);
		}
		sqlite3_finalize(sqlStatement);
	}
	return item;
}

-(void)createObject:(Alumno *)item {
	NSString *sqlInsert = [NSString stringWithFormat:@"Insert into Alumno(NOMBRE, DNI, NOTA, CHICO) VALUES ('%@', '%@', '%@', '%ld')", item.nombre, item.dni, item.nota, (long)item.chico];


	const char *sql = [sqlInsert UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

- (void) updateObject:(Alumno*)item{


	const char *sql = [[NSString stringWithFormat:@"update Alumno set nombre = '%@', dni = '%@', nota = '%ld', chico = '%ld' where id=%ld" , item.nombre, item.dni, item.nota, item.chico, item.myid] UTF8String];

	sqlite3_stmt *sqlStatement;

	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}

}

- (void) deleteObject:(NSInteger)auxid{

	const char *sql = [[NSString stringWithFormat:@"delete from Alumno where myid=%ld",auxid] UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

@end