#import <Foundation/Foundation.h>
#import <sqlite3.h>
#import "Alumno.h"

@interface AlumnoDAO : NSObject{
	sqlite3 *db;
}

+ (Alumno *) instance;

- (void) createObject:(Alumno*) item;

- (NSMutableArray *) getAll;

- (NSMutableArray *) getById:(NSInteger)auxid;

- (void) updateObject:(Alumno*) item;

- (void) deleteObject:(NSInteger)auxid;

@end