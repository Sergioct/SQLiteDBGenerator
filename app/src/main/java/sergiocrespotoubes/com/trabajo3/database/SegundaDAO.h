#import <Foundation/Foundation.h>
#import <sqlite3.h>
#import "Segunda.h"

@interface SegundaDAO : NSObject{
	sqlite3 *db;
}

+ (Segunda *) instance;

- (void) createObject:(Segunda*) item;

- (NSMutableArray *) getAll;

- (NSMutableArray *) getById:(NSInteger)auxid;

- (void) updateObject:(Segunda*) item;

- (void) deleteObject:(NSInteger)auxid;

@end